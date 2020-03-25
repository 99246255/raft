package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2018/9/11 11:45
 * @Description: 投票rpc返回值
 */
public class VoteResponse {

    /**
     * 节点任期号
     */
    private long term;
    /**
     * 候选人赢得了此张选票时为true
     */
    private boolean granted;

    public VoteResponse(long term, boolean granted) {
        this.term = term;
        this.granted = granted;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }
}
