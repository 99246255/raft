package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2018/9/11 11:39
 * @Description: 投票rpc请求参数
 */
public class VoteRequest {
    private String serverId;
    private long term;
    private long lastLogTerm;
    private long lastLogIndex;
    private String groupId;

    public VoteRequest(String serverId, long term, long lastLogTerm, long lastLogIndex, String groupId) {
        this.serverId = serverId;
        this.term = term;
        this.lastLogTerm = lastLogTerm;
        this.lastLogIndex = lastLogIndex;
        this.groupId = groupId;
    }

    /**
     * 节点id
     * @return
     */
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * 任期号
     * @return
     */
    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    /**
     * 候选人的最后日志条目的任期号
     * @return
     */
    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    /**
     * 候选人最后日志条目的索引值
     * @return
     */
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
