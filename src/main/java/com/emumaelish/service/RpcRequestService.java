package com.emumaelish.service;

import com.emumaelish.entity.*;

/**
 * @Author: enumaelish
 * @Date: 2018/9/7 13:46
 * @Description: rpc请求发起接口
 */
public interface RpcRequestService {

    /**
     * 向其他节点发起投票rpc
     */
    public VoteResponse vote(String serverId, VoteRequest voteRequest);

    /**
     * 向其他节点发起预投票rpc
     */
    public VoteResponse preVote(String serverId, VoteRequest voteRequest);

    /**
     * 向其他节点发起心跳/日志复制
     */
    public AppendEntriesResponse appendEntries(String serverId, AppendEntriesRequest request);


    /**
     * 向其他节点发起快照请求
     */
    public void installSnapshot();

    public void addChannel(PeerId peerId);
}
