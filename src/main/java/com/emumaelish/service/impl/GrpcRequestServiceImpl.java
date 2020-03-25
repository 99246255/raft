package com.emumaelish.service.impl;

import com.emumaelish.common.JsonUtil;
import com.emumaelish.config.RaftOptions;
import com.emumaelish.entity.*;
import com.emumaelish.exception.ConnectionException;
import com.emumaelish.grpc.config.GrpcChannelFactory;
import com.emumaelish.service.RpcRequestService;
import io.grpc.StatusRuntimeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author: enumaelish
 * @Date: 2018/9/7 14:05
 * @Description: grpc实现
 */
@ConditionalOnProperty(name = "grpc.enable", havingValue = "true")
@Service
@Log4j2
public class GrpcRequestServiceImpl implements RpcRequestService {


    @Autowired
    GrpcChannelFactory grpcChannelFactory;

    @Autowired
    RaftOptions raftOptions;
    @Override
    public VoteResponse vote(String serverId, VoteRequest voteRequest) {
        log.debug("向【" + serverId + "】投票请求参数: " + JsonUtil.toJSONString(voteRequest));
        VoteResponse voteResponse = getVoteType(serverId, voteRequest, false);
        log.debug("向【" + serverId + "】投票请求返回： " + JsonUtil.toJSONString(voteResponse));
        return voteResponse;
    }

    private VoteResponse getVoteType(String serverId, VoteRequest voteRequest,boolean isPre){
        com.enumaelish.grpc.proto.RpcMessage.VoteRequest request = com.enumaelish.grpc.proto.RpcMessage.VoteRequest.newBuilder()
                .setTerm(voteRequest.getTerm())
                .setServerId(voteRequest.getServerId())
                .setLastLogIndex(voteRequest.getLastLogIndex())
                .setLastLogTerm(voteRequest.getLastLogTerm())
                .setGroupId(voteRequest.getGroupId())
                .build();
        com.enumaelish.grpc.proto.RaftServiceGrpc.RaftServiceBlockingStub raftServiceBlockingStub = grpcChannelFactory.getStub(serverId);
        com.enumaelish.grpc.proto.RpcMessage.VoteResponse response = null;
        try {
            if(isPre) {
                response = raftServiceBlockingStub.prevote(request);
            }else{
                response = raftServiceBlockingStub.vote(request);
            }
        } catch (Exception e) {
            if(e instanceof StatusRuntimeException){
                throw new ConnectionException(e.getMessage());
            }
            throw e;
        }
        VoteResponse voteResponse = new VoteResponse(response.getTerm(), response.getGranted());
        return voteResponse;
    }
    @Override
    public VoteResponse preVote(String serverId, VoteRequest voteRequest) {
        log.debug("向【" + serverId + "】预投票请求参数: " + JsonUtil.toJSONString(voteRequest));
        VoteResponse voteResponse = getVoteType(serverId, voteRequest, true);
        log.debug("向【" + serverId + "】预投票请求返回： " + JsonUtil.toJSONString(voteResponse));
        return voteResponse;
    }

    @Override
    public AppendEntriesResponse appendEntries(String serverId, AppendEntriesRequest request) {
        log.debug("向【" + serverId + "】日志复制请求参数: " + JsonUtil.toJSONString(request));
        // 将AppendEntriesRequest转化为RpcMessage.AppendEntriesRequest
        com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest.Builder builder = com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest.newBuilder()
                .setTerm(request.getTerm())
                .setServerId(request.getServerId())
                .setPrevLogIndex(request.getPrevLogIndex())
                .setGroupId(request.getGroupId())
                .setPrevLogTerm(request.getPrevLogTerm())
                .setLeaderCommit(request.getLeaderCommit());
        List<LogEntry> entries = request.getEntries();
        for (LogEntry logEntry : entries) {
            com.enumaelish.grpc.proto.RpcMessage.LogEntry entry = com.enumaelish.grpc.proto.RpcMessage.LogEntry.newBuilder()
                    .setTypeValue(logEntry.getType().getType())
                    .setTerm(logEntry.getTerm())
                    .setData(logEntry.getData() == null? null : logEntry.getData())
                    .setIndex(logEntry.getIndex())
                    .build();
            builder.addEntries(entry);
        }
        // 获取通道
        com.enumaelish.grpc.proto.RaftServiceGrpc.RaftServiceBlockingStub raftServiceBlockingStub = grpcChannelFactory.getStub(serverId);
        com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse response = null;
        try {
            response = raftServiceBlockingStub.appendEntries(builder.build());
        } catch (Exception e) {
            if(e instanceof StatusRuntimeException){
                throw new ConnectionException(e.getMessage());
            }
            throw e;
        }
        // 构造返回参数
        AppendEntriesResponse response1 = new AppendEntriesResponse(response.getSuccess(), response.getTerm(), response.getLastLogIndex());
        log.debug("向【" + serverId + "】日志复制请求返回: " + JsonUtil.toJSONString(response1));
        return response1;
    }

    @Override
    public void installSnapshot() {

    }

    @Override
    public void addChannel(PeerId peerId) {
        grpcChannelFactory.addChannel(peerId);
    }
}
