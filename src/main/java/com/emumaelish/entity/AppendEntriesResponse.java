package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2018/9/11 14:40
 * @Description: 日志复制请求返回
 */
public class AppendEntriesResponse {
    /**
     * 跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真
     */
    private boolean success;
    /**
     * 当前的任期号，用于领导人去更新自己
     */
    private long term;
    /**
     * 该节点最新的日志索引
     */
    private long lastLogIndex;

    public AppendEntriesResponse(boolean success, long term, long lastLogIndex) {
        this.success = success;
        this.term = term;
        this.lastLogIndex = lastLogIndex;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }
}
