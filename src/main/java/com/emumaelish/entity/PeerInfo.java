package com.emumaelish.entity;


import lombok.Data;

/**
 * @Author: enumaelish
 * @Description: 其他节点状态，由leader维护
 */
@Data
public class PeerInfo {

    // 需要发送给follower的下一个日志条目的索引值，只对leader有效
    private long nextIndex;
    // 已复制日志的最高索引值
    private long matchIndex;
    // 节点信息
    private PeerId peerId;

    public PeerInfo() {
    }

    public PeerInfo(PeerId peerId) {
        this.peerId = peerId;
        this.nextIndex = 1L;
        this.matchIndex = 0L;
    }

}
