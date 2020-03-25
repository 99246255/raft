package com.emumaelish.service.impl;

import com.emumaelish.entity.*;
import com.emumaelish.grpc.config.GrpcChannelFactory;
import com.emumaelish.service.RaftEngine;
import com.emumaelish.service.RaftNodeService;
import com.emumaelish.service.StateMachine;
import com.emumaelish.service.StoreService;
import com.enumaelish.grpc.proto.RpcMessage;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * 必须等节点信息初始化之后才能开启grpc服务
 */
@Service
@GrpcService
@Order(10)
public class GRpcServletImpl extends com.enumaelish.grpc.proto.RaftServiceGrpc.RaftServiceImplBase {

    Logger logger = LoggerFactory.getLogger(GRpcServletImpl.class);

    @Autowired
    RaftNodeService raftNodeService;

    @Autowired
    RaftEngine raftEngine;

    @Autowired
    StoreService storeService;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    GrpcChannelFactory grpcChannelFactory;

    @Override
    public void vote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request, StreamObserver<RpcMessage.VoteResponse> responseObserver) {
        com.enumaelish.grpc.proto.RpcMessage.VoteResponse response = getVoteResponse(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void prevote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request, StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> responseObserver) {
        com.enumaelish.grpc.proto.RpcMessage.VoteResponse response = getPreVoteResponse(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理rpc投票请求，接收投票请求
     * @param request
     * @return
     */
    private com.enumaelish.grpc.proto.RpcMessage.VoteResponse getPreVoteResponse(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request){
        Node raftNode = raftNodeService.getNodeByGroupId(request.getGroupId());
        if(raftNode == null){
            return com.enumaelish.grpc.proto.RpcMessage.VoteResponse.newBuilder().setGranted(false).setTerm(0L).build();
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            com.enumaelish.grpc.proto.RpcMessage.VoteResponse.Builder responseBuilder = com.enumaelish.grpc.proto.RpcMessage.VoteResponse.newBuilder();
            responseBuilder.setGranted(false);
            long currentTerm = raftNode.getCurrentTerm();
            responseBuilder.setTerm(currentTerm);
            if (!raftNode.getPeerMap().containsKey(request.getServerId())) {
                // 请求节点不在配置节点中，拒绝投票
                return responseBuilder.build();
            }
            if (request.getTerm() < currentTerm) {
                // 请求节点比当前节点term小，拒绝投票
                return responseBuilder.build();
            }
            Ballot ballot = raftEngine.getPreVoteBallot(raftNode);
            if (request.getTerm() > currentTerm) {
                // 请求节点比当前节点term大，清空投票信息，变成follower，并重置选举时间
                ballot.setVotedFor(null);
                ballot.setVoteCount(0);
            }
            LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
            boolean logIsOk = request.getLastLogTerm() > lastLogEntry.getTerm()
                    || (request.getLastLogTerm() == lastLogEntry.getTerm()
                    && request.getLastLogIndex() >= lastLogEntry.getIndex());
            if (ballot.getVotedFor() == null && logIsOk) {
                // 获得投过票， 请求中任期号比当前日志的大或者任期号相同，偏移量更大
                ballot.setVotedFor(PeerId.parsePeer(request.getServerId()));
                responseBuilder.setGranted(true);
                responseBuilder.setTerm(currentTerm);
                raftEngine.resetElectionTimer(raftNode);// 需要重置选举超时时间
            }
            // 拒绝投票，已投过或者偏移量比目标的大
            return responseBuilder.build();
        } finally {
            lock.unlock();
        }

    }


    @Override
    public void appendEntries(com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request, StreamObserver<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> responseObserver) {
        com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse response = getAppendEntriesResponse(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理rpc投票请求，接收投票请求
     * @param request
     * @return
     */
    private com.enumaelish.grpc.proto.RpcMessage.VoteResponse getVoteResponse(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request){
        Node raftNode = raftNodeService.getNodeByGroupId(request.getGroupId());
        if(raftNode == null){
            return com.enumaelish.grpc.proto.RpcMessage.VoteResponse.newBuilder().setGranted(false).setTerm(0L).build();
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            com.enumaelish.grpc.proto.RpcMessage.VoteResponse.Builder responseBuilder = com.enumaelish.grpc.proto.RpcMessage.VoteResponse.newBuilder();
            responseBuilder.setGranted(false);
            responseBuilder.setTerm(raftNode.getCurrentTerm());
            if (!raftNode.getPeerMap().containsKey(request.getServerId())) {
                // 请求节点不在配置节点中，拒绝投票
                return responseBuilder.build();
            }
            if (request.getTerm() < raftNode.getCurrentTerm()) {
                // 请求节点比当前节点term小，拒绝投票
                return responseBuilder.build();
            }
            if (request.getTerm() > raftNode.getCurrentTerm()) {
                // 请求节点比当前节点term大，清空投票信息，变成follower，并重置选举时间
                raftEngine.becomeFollower(raftNode, request.getTerm());
            }
            LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
            boolean logIsOk = request.getLastLogTerm() > lastLogEntry.getTerm()
                    || (request.getLastLogTerm() == lastLogEntry.getTerm()
                    && request.getLastLogIndex() >= lastLogEntry.getIndex());
            Ballot ballot = raftEngine.getVoteBallot(raftNode);
            if (ballot.getVotedFor() == null && logIsOk) {
                // 获得投过票， 请求中任期号比当前日志的大或者任期号相同，偏移量更大
                ballot.setVotedFor(PeerId.parsePeer(request.getServerId()));
                responseBuilder.setGranted(true);
                responseBuilder.setTerm(raftNode.getCurrentTerm());
            }
            // 拒绝投票，已投过或者偏移量比目标的大
            return responseBuilder.build();
        } finally {
            lock.unlock();
        }

    }


    /**
     * 处理日志复制请求
     * @param request
     * @return
     */
    private com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse getAppendEntriesResponse(com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request){
        Node raftNode = raftNodeService.getNodeByGroupId(request.getGroupId());
        if(raftNode == null){
            return com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.newBuilder().setSuccess(false).setTerm(0L).setLastLogIndex(0L).build();
        }
        // 检查节点是否非法节点
        if(raftNode.getSlaveMap().containsKey(request.getServerId()) || !raftNode.getPeerMap().containsKey(request.getServerId())){
            return com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.newBuilder().setSuccess(false).setTerm(0L).setLastLogIndex(0L).build();
        }
        if(raftNode.getIsProcess().get()){
            raftEngine.resetElectionTimer(raftNode);// 防止处理过程过长时发生视图变化
            return com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.newBuilder().setSuccess(false).setTerm(raftNode.getCurrentTerm()).setLastLogIndex(request.getPrevLogIndex()).build();
        }
        Lock lock = raftNode.getLock();
        lock.lock();
        try {
            raftNode.setIsProcess(new AtomicBoolean(true));
            LogEntry lastLogEntry = storeService.getLastLogEntry(raftNode.getGroupId());
            long lastLogEntryIndex = lastLogEntry.getIndex();
            // 默认不成功
            com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.Builder builder = com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.newBuilder()
                    .setLastLogIndex(lastLogEntryIndex)
                    .setSuccess(false)
                    .setTerm(raftNode.getCurrentTerm());
            long term = request.getTerm();
            if(raftNode.getCurrentTerm() > term){
                // 忽略比当前节点term小的请求
                return builder.build();
            }
            // 设置follower状态，如果term比当前节点大，需清空节点投票，leader信息
            raftEngine.becomeFollower(raftNode, term);
            if(raftNode.getLeaderId() == null){
                // 设置leader信息
                raftNode.setLeaderId(PeerId.parsePeer(request.getServerId()));
            }
            if (!request.getServerId().equals(raftNode.getLeaderId().toString())) {
                // term相同，leader不一样，不知道啥情况会出现，目前把当前节点变成follower，并增大term值
                logger.warn("节点{} 成为leader, 任期号相同为{} 之前的leader为{}",
                        request.getServerId(), request.getTerm(), raftNode.getLeaderId());
                raftEngine.becomeFollower(raftNode, term + 1);
                builder.setTerm(term + 1);
                return builder.build();
            }
            long prevLogIndex = request.getPrevLogIndex();
            if (prevLogIndex > lastLogEntryIndex) {
                // 服务端保存的该节点偏移量过大，返回当前节点实际偏移量
                logger.debug("服务端保存的该节点{}的最新日志偏移量过后，请求偏移量{}，本地的偏移量{}",
                        prevLogIndex, lastLogEntryIndex);
                return builder.build();
            }
            if(prevLogIndex  < raftNode.getCommitIndex()){
                // 日志偏移量小于当前节点的提交量
                builder.setLastLogIndex(raftNode.getCommitIndex());
                return builder.build();
            }
            LogEntry firstLogEntry = storeService.getFirstLogEntry(raftNode.getGroupId());

            LogEntry logEntry = storeService.getLogEntryByLogIndex(raftNode.getGroupId(), prevLogIndex);
            if (prevLogIndex >= firstLogEntry.getIndex()
                    && logEntry != null && logEntry.getTerm()
                    != request.getPrevLogTerm()) {
                // 偏移量相同但是任期不同，告诉服务端向前偏移
                logger.debug("请求偏移量{}的日志任期号{}, 本地此偏移量的任期号{}",
                        request.getPrevLogTerm(), prevLogIndex,
                        logEntry.getTerm());
                // 偏移量-1
                builder.setLastLogIndex(prevLogIndex - 1);
                return builder.build();
            }
            if(logEntry == null && prevLogIndex >=1){
                // 前一日志为空，偏移量从1开始的，告诉服务端向前偏移
                logger.error("请求偏移量{}的日志任期号{}, 本地此偏移量的日志不存在",
                        request.getPrevLogTerm(), prevLogIndex);
                builder.setLastLogIndex(prevLogIndex - 1);
                return builder.build();
            }
            if(prevLogIndex  < raftNode.getLastApplied()){
                // 日志偏移量小于当前节点的提交量
                builder.setLastLogIndex(raftNode.getLastApplied());
                return builder.build();
            }
            builder.setSuccess(true);
            if (request.getEntriesCount() == 0) {
                // 心跳请求
                logger.debug("收到心跳请求，leader{}任期号{}",
                        request.getServerId(), raftNode.getCurrentTerm());
                builder.setTerm(raftNode.getCurrentTerm());
                // 更新状态机
                updateStoreMachine(raftNode, request);
                return builder.build();
            }
            // 开始处理日志复制请求
            List<LogEntry> entries = new ArrayList<>();
            long index = prevLogIndex;
            for (com.enumaelish.grpc.proto.RpcMessage.LogEntry entry : request.getEntriesList()) {
                index++;
                if(entry.getIndex() != index){
                    // 可能是发请求的时候并发量大导致
                    builder.setLastLogIndex(prevLogIndex - 1);
                    return builder.build();
                }
                LogEntry logEntry1 = new LogEntry();
                logEntry1.setType(LogEntryType.getEntryType(entry.getTypeValue()));
                logEntry1.setTerm(entry.getTerm());
                logEntry1.setIndex(entry.getIndex());
                logEntry1.setData(entry.getData());
                entries.add(logEntry1);
            }
            // 存储日志
            storeService.addLogEntrys(raftNode.getGroupId(), entries);
            builder.setLastLogIndex(index);
            logger.debug("复制日志成功到" + builder.getLastLogIndex());
            // 更新状态机
            updateStoreMachine(raftNode, request);
            return builder.build();
        } finally {
            raftNode.setIsProcess(new AtomicBoolean(false));
            lock.unlock();
        }
    }

    // 追随者节点保存自身日志的偏移量以及状态机更新情况
    private void updateStoreMachine(Node raftNode, com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request) {
        // 获取提交的日志偏移量
        long newCommitIndex = Math.min(request.getLeaderCommit(),
                request.getPrevLogIndex() + request.getEntriesCount());
        raftNode.setCommitIndex(newCommitIndex);
        if (raftNode.getLastApplied() < newCommitIndex) {
            // 更新状态机
            for (long index = raftNode.getLastApplied() + 1; index <= newCommitIndex; index++) {
                raftEngine.updateLogEntry(raftNode, storeService.getLogEntryByLogIndex(raftNode.getGroupId(), index), true);
            }
            raftNode.setLastApplied(newCommitIndex);
        }
        storeService.saveRaftNode(raftNode);
    }
    @Override
    public void installSnapshot(com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest request, StreamObserver<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> responseObserver) {
        super.installSnapshot(request, responseObserver);
    }





}
