package com.emumaelish.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: enumaelish
 * @Date: 2019/5/29 20:35
 * @Description: 需要存储的节点信息
 */
@Data
public class NodeStore implements Serializable {

    private long currentTerm = 0l;

    // 已知的最大的已经被提交的日志条目的索引值
    private long commitIndex = 0L;

    // 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增）
    private volatile long lastApplied = 0l;

    private PeerId peerId;

    // 所属分组
    private String groupId;

    /**
     * 最新的日志配置偏移量
     */
    private long configIndex = 0L;

    /**
     * 共识节点状态
     */
    private Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();

    /**
     * 同步节点状态
     */
    private Map<String, PeerInfo> slaveMap = new ConcurrentHashMap<>();
}
