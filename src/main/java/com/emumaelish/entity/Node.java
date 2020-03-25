package com.emumaelish.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.emumaelish.common.JsonUtil;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: enumaelish
 * @Date: 2019/5/27 11:26
 * @Description:
 */
@Data
public class Node {

    // 节点状态
    private NodeState state = NodeState.FOLLOWER;

    // 在当前获得选票的候选人的Id
//    private PeerId votedFor;

    // leader节点id
    private PeerId leaderId;

//    // 投票数
//    private Integer voteCount = 0;

    // 服务器最后一次知道的任期号（初始化为 0，持续递增）
    private long currentTerm = 0l;

    // 已知的最大的已经被提交的日志条目的索引值
    private long commitIndex = 0L;

    // 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
    private volatile long lastApplied = 0L;

    private PeerId peerId;

    // 所属分组
    private String groupId;

    /**
     * 最新的日志配置偏移量
     */
    private long configIndex = 0L;
    /**
     * 标记是否在处理日志复制，正在处理直接返回
     */
    @JSONField(serialize = false)
    public AtomicBoolean isProcess = new AtomicBoolean(false);

    /**
     * 状态锁，修改属性时必须加锁
     */
    @JSONField(serialize = false)
    private Lock lock = new ReentrantLock();

    @JSONField(serialize = false)
    private Condition commitIndexCondition = lock.newCondition();

    /**
     * 共识节点状态
     */
    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    /**
     * 同步节点状态
     */
    private Map<String, PeerInfo> slaveMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Node node = new Node();
        node.setCommitIndex(1L);
        node.setCurrentTerm(2L);
        String peerIdstr = "localhost:1111:0:0";
        PeerId peerId = PeerId.parsePeer(peerIdstr);
        node.setPeerId(peerId);
        PeerInfo peer = new PeerInfo();
        peer.setMatchIndex(3L);
        peer.setNextIndex(2L);
        peer.setPeerId(PeerId.parsePeer("localhost:1112:0:1"));
        node.getPeerMap().put(peerIdstr, peer);
        String string = JsonUtil.toJSONString(node);

        System.out.println(string);
    }
}
