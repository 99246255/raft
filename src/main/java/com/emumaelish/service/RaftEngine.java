package com.emumaelish.service;

import com.emumaelish.common.JsonUtil;
import com.emumaelish.config.RaftOptions;
import com.emumaelish.entity.*;
import com.emumaelish.entity.conf.Configuration;
import com.emumaelish.entity.conf.ConfigurationEntry;
import com.emumaelish.entity.conf.ConfigurationLogEntry;
import com.emumaelish.entity.conf.ConfigurationManager;
import com.emumaelish.exception.ConnectionException;
import com.emumaelish.util.LogThreadPoolExecutor;
import com.emumaelish.util.NamedThreadFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;

/**
 * @Author: enumaelish
 * @Date: 2018/9/7 10:38
 * @Description: raft 算法引擎，节点状态变化，投票，日志复制等实现
 */
@Service
@Log4j2
@Order(0)
public class RaftEngine {

    @Autowired
    protected RaftOptions raftOptions;

    @Autowired
    protected StoreService storeService;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    RpcRequestService rpcRequestService;

    @Autowired
    RaftNodeService raftNodeService;

    /**
     * rpc请求线程池
     */
    protected ThreadPoolExecutor voteEcutorService;

    /**
     * rpc请求线程池
     */
    protected ThreadPoolExecutor appendExecutorService;

    /**
     * 定时任务线程池，心跳和选举
     */
    protected ScheduledExecutorService scheduledExecutorService;

    /**
     * 默认线程池拒绝处理任务时的策略
     */
    private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * 选举任务
     */
    static ConcurrentHashMap<String, ScheduledFuture> electionScheduledMap = new ConcurrentHashMap<>();

    /**
     * 心跳任务
     */
    static ConcurrentHashMap<String, ScheduledFuture> heartbeatScheduledMap = new ConcurrentHashMap<>();

    /**
     * 节点配置
     */
    static ConcurrentHashMap<String, ConfigurationManager> configMap = new ConcurrentHashMap<>();

    /**
     * 各个节点投票情况
     */
    static Map<String, Ballot> voteMap = new ConcurrentHashMap<>();

    /**
     * 预投票情况
     */
    static Map<String, Ballot> prevoteMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化rpc线程池
        voteEcutorService = new LogThreadPoolExecutor(
                4,
                raftOptions.getRaftConsensusThreadNum(),
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4096),
                new NamedThreadFactory("vote-"),
                defaultHandler, "vote");
        appendExecutorService = new LogThreadPoolExecutor(
                4,
                raftOptions.getRaftConsensusThreadNum(),
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4096),
                new NamedThreadFactory("appendlog-"),
                defaultHandler, "appendlog");
        // 初始化定时任务线程池
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
        //加载节点信息，需在线程池初始化之后
        raftNodeService.init();
        Collection<Node> values = raftNodeService.getAllNodes();
        for(Node raftNode: values){
            if (raftNode.getCommitIndex() < raftNode.getLastApplied()) {// 处理错误数据
                raftNode.setCommitIndex(raftNode.getLastApplied());
                storeService.saveRaftNode(raftNode);
            }
            LogEntry logEntry = storeService.getLogEntryByLogIndex(raftNode.getGroupId(), raftNode.getConfigIndex());
            if(logEntry == null || !LogEntryType.CONFIGURATION.equals(logEntry.getType())){
                // 如果出现这种bug，可能服务内部被攻击，数据被篡改了，清空后从其他节点重新同步，如果还不行，只能删库跑路了
                throw new IllegalArgumentException("数据有误，找不到节点配置日志");
            }
            addConfigurationEntry(raftNode.getGroupId(), getConfigurationEntryByLogEntry(logEntry));
        }
    }

    /**
     * 启动后开启心跳
     */
    public void start(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collection<Node> values = raftNodeService.getAllNodes();
        for(Node node: values ) {
            resetElectionTimer(node);
        }
    }

    public ConfigurationEntry getConfigurationEntryByLogEntry(LogEntry logEntry){
        if(logEntry == null){
            Configuration conf = new Configuration();
            return new ConfigurationEntry(0, 0, conf, conf);
        }
        String data = logEntry.getData();
        ConfigurationLogEntry entry = JsonUtil.toBean(data, ConfigurationLogEntry.class);
        if(entry == null){
            Configuration conf = new Configuration();
            return new ConfigurationEntry(0, 0, conf, conf);
        }
        Configuration conf = new Configuration();
        boolean parse = conf.parse(entry.getConf());
        if(!parse){
            throw new IllegalArgumentException("数据有误，节点配置日志格式有误");
        }
        Configuration oldConf = new Configuration();
        parse = oldConf.parse(entry.getOldConf());
        if(!parse){
            throw new IllegalArgumentException("数据有误，节点配置日志格式有误");
        }
        return new ConfigurationEntry(logEntry.getTerm(), logEntry.getIndex(), conf, oldConf);
    }

    /**
     * 候选人变领导人，设置节点状态和leaderId
     */
    protected void candidateToLeader(Node raftNode) {
        // 已是leader不做处理
        if(NodeState.LEADER.equals(raftNode.getState())){
            return;
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            raftNode.setState(NodeState.LEADER);
            raftNode.setLeaderId(raftNode.getPeerId());
            storeService.saveRaftNode(raftNode);
            log.info(new StringBuffer(raftNode.getGroupId()).append("-选举出leader: )").append(raftNode.getPeerId().toString()));
            // start heartbeat timer
            appendEntries(raftNode);
            resetElectionTimer(raftNode);
        } catch (Exception e) {
            e.printStackTrace();
            raftNode.setState(NodeState.CANDIDATE);
            raftNode.setLeaderId(null);
            return;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取预投票信息
     * @param raftNode
     * @return
     */
    public Ballot getPreVoteBallot(Node raftNode){
        if(prevoteMap.containsKey(raftNode.getGroupId())){
            return prevoteMap.get(raftNode.getGroupId());
        }else{
            Ballot ballot = new Ballot();
            ballot.setVoteCount(0);
            ballot.setVotedFor(null);
            prevoteMap.put(raftNode.getGroupId(), ballot);
            return ballot;
        }
    }

    /**
     * 获取投票信息
     * @param raftNode
     * @return
     */
    public Ballot getVoteBallot(Node raftNode){
        if(voteMap.containsKey(raftNode.getGroupId())){
            return voteMap.get(raftNode.getGroupId());
        }else{
            Ballot ballot = new Ballot();
            ballot.setVoteCount(0);
            ballot.setVotedFor(null);
            voteMap.put(raftNode.getGroupId(), ballot);
            return ballot;
        }
    }

    /**
     * 投票成功，投票数+1
     */
    protected Ballot addvoteCount(Node raftNode) {
        Ballot ballot = getPreVoteBallot(raftNode);
        Lock lock = ballot.getLock();
        lock.lock();
        if (!raftNode.getPeerId().equals(ballot.getVotedFor())) {
            return ballot;
        }
        ballot.setVoteCount(ballot.getVoteCount() + 1);
        prevoteMap.put(raftNode.getGroupId(), ballot);
        lock.unlock();
        return ballot;
    }


    /**
     * 重置选举定时器
     */
    public void resetElectionTimer(Node raftNode) {
        // 取消之前的选举任务
        synchronized (raftNode.getGroupId().intern()) {
            ScheduledFuture electionScheduledFuture = electionScheduledMap.get(raftNode.getGroupId());
            if (electionScheduledFuture != null && !electionScheduledFuture.isDone()) {
                electionScheduledFuture.cancel(true);
            }
            // 创建新的选举任务
            electionScheduledFuture = scheduledExecutorService.schedule(() -> preVoteRequest(raftNode),
                    getElectionTimeoutMs(), TimeUnit.MILLISECONDS);
            electionScheduledMap.put(raftNode.getGroupId(), electionScheduledFuture);
        }
    }

    /**
     * 获取随机选举超时时间
     * @return
     */
    protected int getElectionTimeoutMs() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomElectionTimeout = raftOptions.getElectionTimeoutMilliseconds()
                + random.nextInt(0, raftOptions.getElectionTimeoutMilliseconds());
        log.debug("选举超时时间在{" + randomElectionTimeout + "} ms 之后");
        return randomElectionTimeout;
    }

    /**
     * 重置心跳任务
     */
    protected void resetHeartbeatTimer(Node raftNode) {
        synchronized (raftNode.getGroupId().intern()) {
            ScheduledFuture heartbeatScheduledFuture = heartbeatScheduledMap.get(raftNode.getGroupId());
            if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
                heartbeatScheduledFuture.cancel(true);
            }
            heartbeatScheduledFuture = scheduledExecutorService.schedule(() -> appendEntries(raftNode),
                    raftOptions.getHeartbeatPeriodMilliseconds(), TimeUnit.MILLISECONDS);
            heartbeatScheduledMap.put(raftNode.getGroupId(), heartbeatScheduledFuture);
        }
    }

    /**
     * 变追随者，投票过程收到高版本term，同时刷新选举超时时间，由调用方控制锁
     * @param term     任期号
     */
    public void becomeFollower(Node raftNode, long term) {
        long currentTerm = raftNode.getCurrentTerm();
        if (currentTerm > term) {
            return;
        }
        String copy = JsonUtil.toJSONString(raftNode);
        try {
            if(currentTerm == term && NodeState.FOLLOWER.equals(raftNode.getState())){
                return;//没有变化不需要save，减少一次本地化，但是需要resetElectionTimer
            }
            if (currentTerm < term) {
                // term值比新的小，变成follower,并重置各种参数
                Ballot ballot = getVoteBallot(raftNode);
                raftNode.setCurrentTerm(term);
                ballot.setVotedFor(null);
                raftNode.setLeaderId(null);
                ballot.setVoteCount(0);
                log.info("节点【" + raftNode.getPeerId() + "】收到高版本term，由【" + raftNode.getState().toString() + "】变追随者，高版本任期：" + term);
            }
            raftNode.setState(NodeState.FOLLOWER);
            storeService.saveRaftNode(raftNode);
            // 取消心跳任务
            synchronized (raftNode.getGroupId()) {
                ScheduledFuture heartbeatScheduledFuture = heartbeatScheduledMap.get(raftNode.getGroupId());
                if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
                    heartbeatScheduledFuture.cancel(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Node node = JsonUtil.toBean(copy, Node.class);
            BeanUtils.copyProperties(node, raftNode);
        }finally {
            resetElectionTimer(raftNode);
        }
    }

    /**
     * 日志提交，更新状态机
     * in lock, for leader
     */
    public void advanceCommitIndex(Node raftNode) {
        // 获取quorum matchIndex
        Map<String, PeerInfo> peers = raftNode.getPeerMap();
        int peerNum = peers.size();
        // 记录各个节点的日志偏移量
        long[] matchIndexes = new long[peerNum];
        int i = 0;
        for (PeerInfo peer : peers.values()) {
            if (!peer.getPeerId().equals(raftNode.getPeerId())) {
                matchIndexes[i++] = peer.getMatchIndex();
            }
        }
        LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
        matchIndexes[i] = lastLogEntry.getIndex();
        // 排序
        Arrays.sort(matchIndexes);
        long newCommitIndex = matchIndexes[(peerNum -1) / 2];// 绝大多数节点已提交的日志偏移量
        LogEntry logEntryByLogIndex = storeService.getLogEntryByLogIndex(raftNode.getGroupId(), newCommitIndex);
        if(logEntryByLogIndex == null){
            if(newCommitIndex > 0) {// 一开始的时候一定没有偏移量为0的日志
                log.debug("找不到偏移量为【" + newCommitIndex + "】的日志");
            }
            return;
        }
        if (logEntryByLogIndex.getTerm() != raftNode.getCurrentTerm()) {
            // 不能提交老的任期日志
            log.debug("未达成最新任期复制到了大多数节点，日志偏移量：" + newCommitIndex);
            return;
        }
        if (raftNode.getCommitIndex() >= newCommitIndex) {
            //已是最新的日志，无需修改提交
            return;
        }
        // 同步到状态机，并保存节点最新提交日志状态
        long oldCommitIndex = raftNode.getCommitIndex();
        raftNode.setCommitIndex(newCommitIndex);
        for (long index = oldCommitIndex + 1; index <= newCommitIndex; index++) {
            updateLogEntry(raftNode, storeService.getLogEntryByLogIndex(raftNode.getGroupId(), index), false);
        }
        raftNode.setLastApplied(newCommitIndex);
        // 保存最终状态
        storeService.saveRaftNode(raftNode);
        raftNode.getCommitIndexCondition().signalAll();
    }

    /**
     * 更新日志，如果是节点配置日志，需要更新节点信息
     * @param raftNode
     * @param logEntry
     */
    public void updateLogEntry(Node raftNode, LogEntry logEntry,boolean follower){
        if(logEntry == null){
            return;
        }
        if(LogEntryType.CONFIGURATION.equals(logEntry.getType())){
            // 添加到节点更新历史
            ConfigurationEntry configurationEntry = getConfigurationEntryByLogEntry(logEntry);
            if(follower) {
              addConfigurationEntry(raftNode.getGroupId(), configurationEntry);// 主节点在添加配置的时候加过了
            }
            Configuration included = new Configuration();
            Configuration excluded = new Configuration();
            configurationEntry.getConf().diff(configurationEntry.getOldConf(), included, excluded);
            for (PeerId peer : included.getPeers()){
                // 添加
                PeerInfo peerInfo = new PeerInfo(peer);
                if(NodeType.Consensus.equals(peer.getNodeType())){
                    raftNode.getPeerMap().put(peer.toString(), peerInfo);
                }else{
                    raftNode.getSlaveMap().put(peer.toString(), peerInfo);
                }
                rpcRequestService.addChannel(peer);
            }
            for (PeerId peer : excluded.getPeers()){
                if(NodeType.Consensus.equals(peer.getNodeType())){
                    raftNode.getPeerMap().remove(peer.toString());
                }else{
                    raftNode.getSlaveMap().remove(peer.toString());
                }
            }
            raftNode.setConfigIndex(logEntry.getIndex());
        }else {
            stateMachine.updateToStateMachine(raftNode, logEntry);
        }
    }
    /**
     * 更新节点日志偏移量
     *
     * @param serverId
     * @param lastLogIndex
     */
    protected void updatePeerIndex(Node raftNode, String serverId, long lastLogIndex) {
        String copy = JsonUtil.toJSONString(raftNode);
        try {
            if (raftNode.getPeerMap().containsKey(serverId)) {// 更新共识节点
                PeerInfo peer = raftNode.getPeerMap().get(serverId);
                peer.setMatchIndex(lastLogIndex);
                peer.setNextIndex(lastLogIndex + 1);
                storeService.saveRaftNode(raftNode);
            }else if(raftNode.getSlaveMap().containsKey(serverId)) {// 更新同步节点
                PeerInfo peer = raftNode.getSlaveMap().get(serverId);
                peer.setMatchIndex(lastLogIndex);
                peer.setNextIndex(lastLogIndex + 1);
                storeService.saveRaftNode(raftNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Node node = JsonUtil.toBean(copy, Node.class);
            BeanUtils.copyProperties(node, raftNode);
        }
    }

//    /**
//     * 是否有超过一半连接成功的节点
//     * @return
//     */
//    public boolean moreThanHalfPeerState(Node raftNode){
//        int count = 1; // peers包含自身且始终是false，所以默认从1开始统计
//        for (PeerInfo peer : raftNode.getPeerMap().values()){
//            Boolean b = peersState.get(peer.getPeerId().toString());
//            if(b){
//                count++;
//            }
//        }
//        // 与大部分节点不连通，不增加term, 否则增加term
//        if(count > raftNode.getPeerMap().size()/2){
//            return true;
//        }
//        return false;
//    }
    /**
     * follower状态变成Candidate状态
     *
     * @return
     */
    protected boolean followerToCandidatee(Node raftNode) {
        // 节点在配置中
        if (!raftNode.getPeerMap().containsKey(raftNode.getPeerId().toString())) {
            return false;
        }
        if (NodeState.LEADER.equals(raftNode.getState())) {
            // 是leader，连通超过一半，不会触发重新选举，只有连接不同超过一半才会重新选举
//            if(moreThanHalfPeerState(raftNode)){
                return false;
//            }
        }
        // 预投票请求已经处理了这种情况
//        if (NodeState.CANDIDATE.equals(raftNode.getState())){//已是候选人状态但
//            //统计与其他节点连接状态是不是大部分正常的,正常的不需要自增
//           if(!moreThanHalfPeerState(raftNode)){
//               return true;
//           }
//        }
        String copy = JsonUtil.toJSONString(raftNode);
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            raftNode.setCurrentTerm(raftNode.getCurrentTerm() + 1);
            raftNode.setState(NodeState.CANDIDATE);
            raftNode.setLeaderId(null);
            storeService.saveRaftNode(raftNode);
        } catch (Exception e) {
            e.printStackTrace();
            Node node = JsonUtil.toBean(copy, Node.class);
            BeanUtils.copyProperties(node, raftNode);
            return false;
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 清空投票信息,并初始化投票数1(默认投自己)
     * @param node
     */
    private void resetVote(Node node, boolean isPre){
        Ballot ballot;
        if(isPre){
            ballot = getPreVoteBallot(node);
        }else{
            ballot = getVoteBallot(node);
        }
        Lock lock = ballot.getLock();
        lock.lock();
        ballot.setVotedFor(node.getPeerId());
        ballot.setVoteCount(1);
        voteMap.put(node.getGroupId(), ballot);
        lock.unlock();
    }

    /**
     * 预投票
     * @param raftNode
     * @return
     */
    public boolean preVoteRequest(Node raftNode) {
        synchronized (raftNode.getGroupId().intern()){
            ScheduledFuture heartbeatScheduledFuture = heartbeatScheduledMap.get(raftNode.getGroupId());
            if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
                heartbeatScheduledFuture.cancel(true);
            }
        }
        resetVote(raftNode, true);
        Map<String, PeerInfo> peers = raftNode.getPeerMap();
        LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
        VoteRequest voteRequest = new VoteRequest(raftNode.getPeerId().toString(), raftNode.getCurrentTerm() + 1, lastLogEntry.getTerm(), lastLogEntry.getIndex(), raftNode.getGroupId());
        for (PeerInfo peer : peers.values()) {
            if (peer.getPeerId().equals(raftNode.getPeerId())) {
                continue;// 过滤自身节点
            }
            // 不能submit.否则可能会一直等到结果而导致CPU高
            voteEcutorService.execute(() -> {
                try {
                    // 发起rpc请求
                    VoteResponse response = rpcRequestService.preVote(peer.getPeerId().toString(), voteRequest);
                    long currentTerm = raftNode.getCurrentTerm() + 1;// 预投票term+1
                    if (currentTerm == voteRequest.getTerm() &&  response.isGranted()) {
                        // 投票成功累加投票数
                        Ballot preVoteBallot = addvoteCount(raftNode);
                        if (preVoteBallot.getVoteCount() > (raftNode.getPeerMap().size()) / 2) {
                            voteRequest(raftNode);// 预投票成功后才能变成候选人
                        }
                    }
                } catch (ConnectionException e) {
                    log.error("向" + peer.getPeerId() + "投票请求失败");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        resetElectionTimer(raftNode);
        return true;
    }


    /**
     *发起投票，响应投票结果
     *
     * @return
     */
    public boolean voteRequest(Node raftNode) {
        if(raftNode.isProcess.get()){
            // 正在处理
            resetElectionTimer(raftNode);
            return false;
        }
        // 先停止心跳请求 中断日志复制操作,防止leader变成follower后投票时还有日志复制
        synchronized (raftNode.getGroupId().intern()){
            ScheduledFuture heartbeatScheduledFuture = heartbeatScheduledMap.get(raftNode.getGroupId());
            if (heartbeatScheduledFuture != null && !heartbeatScheduledFuture.isDone()) {
                heartbeatScheduledFuture.cancel(true);
            }
        }
        if (!followerToCandidatee(raftNode)) {
            return false;
        }
        resetVote(raftNode, false);
        Map<String, PeerInfo> peers = raftNode.getPeerMap();
        LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
        VoteRequest voteRequest = new VoteRequest(raftNode.getPeerId().toString(), raftNode.getCurrentTerm(), lastLogEntry.getTerm(), lastLogEntry.getIndex(), raftNode.getGroupId());
        for (PeerInfo peer : peers.values()) {
            if (peer.getPeerId().equals(raftNode.getPeerId())) {
                continue;// 过滤自身节点
            }
            // 不能submit.否则可能会一直等到结果而导致CPU高
            voteEcutorService.execute(() -> {
                try {
                    // 发起rpc请求
                    VoteResponse response = rpcRequestService.vote(peer.getPeerId().toString(), voteRequest);
                    long term = response.getTerm();
                    long currentTerm = raftNode.getCurrentTerm();
                    if (currentTerm != voteRequest.getTerm()
                            || !NodeState.CANDIDATE.equals(raftNode.getState())) {
                        // 请求期间term值改变了，可能是有新的leader或者收到了高版本的请求
                        log.debug("候选人投票期间term值改变了，可能是有新的leader或者收到了高版本的请求");
                        return;
                    }
                    if (term > currentTerm) {
                        // 目标节点term比自身大， 更新term并转变成follow
                        Lock lock = raftNode.getLock();
                        lock.lock();
                        try {
                            becomeFollower(raftNode, term);
                        } finally {
                            lock.unlock();
                        }
                        return;
                    } else {
                        if (response.isGranted()) {
                            // 投票成功累加投票数
                            Ballot voteBallot = addvoteCount(raftNode);
                            if (voteBallot.getVoteCount() > (raftNode.getPeerMap().size()) / 2) {
                                candidateToLeader(raftNode);// 成为leader
                            }
                        } else {
                            log.info(new StringBuilder("【").append(raftNode.getGroupId()).append("】").append(peer.getPeerId().toString()).append("拒绝投票给").append(raftNode.getPeerId().toString()));
                        }
                    }
                } catch (ConnectionException e) {
                    log.error("向" + peer.getPeerId() + "投票请求失败");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        resetElectionTimer(raftNode);
        return true;
    }


    /**
     * 日志复制
     */
    public void appendEntries(Node raftNode) {
        synchronized (raftNode) {
            Map<String, PeerInfo> peers = raftNode.getPeerMap();
            Map<String, PeerInfo> slaveMap = raftNode.getSlaveMap();
            List<PeerInfo> list = new ArrayList<>(peers.size() + slaveMap.size());
            list.addAll(peers.values());
            list.addAll(slaveMap.values());
            for (PeerInfo peer : list) {
                if (peer.getPeerId().equals(raftNode.getPeerId())) {
                    continue;// 过滤自身节点
                }
                // 不能submit.否则可能会一直等到结果而导致CPU高
                appendExecutorService.execute(() -> {
                    try {
                        // TODO 考虑是否加锁
                        LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
                        // 获取该节点最新日志
                        long nextIndex = peer.getNextIndex();// 此字段信息可能会有线程安全问题
                        LogEntry prelogEntry = storeService.getLogEntryByLogIndex(raftNode.getGroupId(), nextIndex - 1);
                        if (prelogEntry == null || nextIndex == 0) {
                            // 设置偏移量为0
                            prelogEntry = storeService.getInitConfigLog(raftNode.getGroupId());
                        }
                        // 设置请求参数
                        AppendEntriesRequest request = new AppendEntriesRequest();
                        request.setTerm(raftNode.getCurrentTerm());
                        request.setServerId(raftNode.getPeerId().toString());
                        request.setPrevLogIndex(prelogEntry.getIndex());
                        request.setPrevLogTerm(prelogEntry.getTerm());
                        request.setGroupId(raftNode.getGroupId());
                        List<LogEntry> logEntryArrayList = new ArrayList<LogEntry>();
                        // 获取待复制的日志
                        long numEntries = packEntries(raftNode.getGroupId(), nextIndex, lastLogEntry, logEntryArrayList);
                        request.setEntries(logEntryArrayList);
                        request.setLeaderCommit(Math.min(raftNode.getCommitIndex(), prelogEntry.getIndex() + numEntries));
                        // 发起rpc请求
                        AppendEntriesResponse response = rpcRequestService.appendEntries(peer.getPeerId().toString(), request);
                        Lock lock = raftNode.getLock();
                        lock.lock();
                        try {
                            long term = response.getTerm();
                            if (term > raftNode.getCurrentTerm()) {
                                // 目标节点term比自身大， 更新term并转变成follow
                                becomeFollower(raftNode, term);
                                return;
                            } else {
                                // 更新节点日志偏移量
                                updatePeerIndex(raftNode, peer.getPeerId().toString(), response.getLastLogIndex());
                                if (response.isSuccess()) {// 复制成功
                                    // TODO 复制日志成功后逻辑可能需修改
                                    // 更新状态机,忽略非共识节点
                                    if (NodeType.Consensus.equals(peer.getPeerId().getNodeType())) {
                                        advanceCommitIndex(raftNode);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            lock.unlock();
                        }
                    } catch (ConnectionException e) {
                    log.error("向" + peer.getPeerId() + "日志复制请求失败");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            resetHeartbeatTimer(raftNode);
            resetElectionTimer(raftNode);
        }
    }

    /**
     *  添加日志，防止一次发送日志过多，分批进行
     * @param nextIndex
     * @param lastLogEntry
     * @param entries
     * @return
     */
    private long packEntries(String groupId, long nextIndex, LogEntry lastLogEntry, List<LogEntry> entries) {
        long lastIndex = Math.min(lastLogEntry.getIndex(),
                nextIndex + raftOptions.getMaxLogEntriesPerRequest() - 1);
        for (long index = nextIndex; index <= lastIndex; index++) {
            LogEntry entry = storeService.getLogEntryByLogIndex(groupId, index);
            if(entry != null) {
                entries.add(entry);
            }
        }
        return entries.size();
    }
    /**
     * 设置
     * @param kvEntity
     * @return
     */
    public String replicate(String groupId, KVEntity kvEntity) {
        Node raftNode = raftNodeService.getNodeByGroupId(groupId);
        if(raftNode == null){
            log.info("groupId错误");// 不是leader不允许添加
            return null;
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        long newLastLogIndex = 0;
        try {
            if (!NodeState.LEADER.equals(raftNode.getState())) {
                log.debug("不是leader");// 不是leader不允许添加
                return null;
            }
            LogEntry logEntry = new LogEntry();
            logEntry.setTerm(raftNode.getCurrentTerm());
            logEntry.setType(LogEntryType.DATA);
            logEntry.setData(kvEntity.toString());
            LogEntry longLogEntryEntry = storeService.getLastLogEntry(groupId);
            logEntry.setIndex(longLogEntryEntry.getIndex() + 1);
            logEntry = storeService.add(raftNode.getGroupId(), logEntry);
            newLastLogIndex = logEntry.getIndex();
//            appendEntries(); // 使用心跳来进行日志复制，减少rpc数
            // 等待日志提交处理成功
            long startTime = System.currentTimeMillis();
            while (raftNode.getLastApplied() < newLastLogIndex) {
//                Thread.sleep(raftOptions.getHeartbeatPeriodMilliseconds());
                if (System.currentTimeMillis() - startTime >= raftOptions.getMaxAwaitTimeout()) {
                    // 超过等待时间，跳出循环
                    break;
                }
                raftNode.getCommitIndexCondition().await(raftOptions.getMaxAwaitTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        log.debug("lastAppliedIndex={} newLastLogIndex={}", raftNode.getLastApplied(), newLastLogIndex);
        if (raftNode.getLastApplied() < newLastLogIndex) {
            return null;
        }
        return kvEntity.getValue();
    }

    /**
     * 变更节点
     */
    public long replicateConfig(String groupId, PeerId peerId, boolean isAdd) {
        long newLastLogIndex = 0;
        Node raftNode = raftNodeService.getNodeByGroupId(groupId);
        if(raftNode == null){
            log.info("groupId错误");// 不是leader不允许添加
            throw new IllegalArgumentException("groupId错误");
        }
        if(peerId == null){
            return newLastLogIndex;
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            if (!NodeState.LEADER.equals(raftNode.getState())) {
                throw new IllegalArgumentException("不是leader");
            }
            if(raftNode.getPeerId().equals(peerId)){
                throw new IllegalArgumentException("不能移除主节点");
            }
            ConfigurationManager configurationManager = configMap.get(groupId);
            LogEntry logEntry = new LogEntry();
            logEntry.setTerm(raftNode.getCurrentTerm());
            logEntry.setType(LogEntryType.CONFIGURATION);
            LogEntry longLogEntryEntry = storeService.getLastLogEntry(groupId);
            logEntry.setIndex(longLogEntryEntry.getIndex() + 1);
            configurationManager.truncateSuffix(logEntry.getIndex());
            ConfigurationEntry lastConfiguration = configurationManager.getLastConfiguration();
            ConfigurationLogEntry configurationLogEntry = new ConfigurationLogEntry();
            Set<PeerId> peerSet = lastConfiguration.getConf().getPeerSet();
            if(isAdd){
                if(peerSet.contains(peerId)){
                    throw new IllegalArgumentException("已包含此节点, 无需添加");
                }
                peerSet.add(peerId);
            }else{
                if(!peerSet.contains(peerId)){
                    throw new IllegalArgumentException("未包含此节点, 无法删除");
                }
                peerSet.remove(peerId);
            }
            Configuration newConfig = new Configuration(peerSet);
            configurationLogEntry.setConf(newConfig.toString());
            configurationLogEntry.setOldConf(lastConfiguration.getConf().toString());
            logEntry.setData(JsonUtil.toJSONString(configurationLogEntry));
            logEntry = storeService.add(raftNode.getGroupId(), logEntry);
            ConfigurationEntry configurationEntry = new ConfigurationEntry(logEntry.getTerm(), logEntry.getIndex(), newConfig, lastConfiguration.getConf());
            addConfigurationEntry(groupId, configurationEntry);
            newLastLogIndex = logEntry.getIndex();
//            appendEntries(); // 使用心跳来进行日志复制，减少rpc数
            // 等待日志提交处理成功
            long startTime = System.currentTimeMillis();
            while (raftNode.getLastApplied() < newLastLogIndex) {
//                Thread.sleep(raftOptions.getHeartbeatPeriodMilliseconds());
                if (System.currentTimeMillis() - startTime >= raftOptions.getMaxAwaitTimeout()) {
                    // 超过等待时间，跳出循环
                    break;
                }
                raftNode.getCommitIndexCondition().await(raftOptions.getMaxAwaitTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        log.debug("lastAppliedIndex={} newLastLogIndex={}", raftNode.getLastApplied(), newLastLogIndex);
        if (raftNode.getLastApplied() < newLastLogIndex) {
            throw new RuntimeException("共识超时，出现此情况成功成功与否情况未知");
        }
        return newLastLogIndex;
    }

    /**
     * 添加节点变更到历史记录，或自动去除未共识成功的记录
     * @param groupId
     * @param configurationEntry
     */
    public void addConfigurationEntry(String groupId, ConfigurationEntry configurationEntry){
        ConfigurationManager configurationManager = configMap.get(groupId);
        if(configurationManager == null){
            configurationManager = new ConfigurationManager();
        }
        configurationManager.truncateSuffix(configurationEntry.getIndex());
        configurationManager.add(configurationEntry);
        configMap.put(groupId, configurationManager);
    }


}
