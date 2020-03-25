package com.emumaelish.grpc.config;

import com.emumaelish.config.GrpcProperties;
import com.emumaelish.entity.PeerId;
import com.enumaelish.grpc.proto.RaftServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 11:14
 * @Description: Channel工厂，和grpc-client-spring-boot-starter中的有差别，去掉了拦截器,添加channel获取
 */
public class GrpcChannelFactory {

    private final GrpcProperties grpcProperties;
    private ConcurrentHashMap<String, ManagedChannel> channels = new ConcurrentHashMap<String, ManagedChannel>();
    private ConcurrentHashMap<String, RaftServiceGrpc.RaftServiceBlockingStub> stubs = new ConcurrentHashMap<String, RaftServiceGrpc.RaftServiceBlockingStub>();
    public GrpcChannelFactory(GrpcProperties grpcProperties) {
        this.grpcProperties = grpcProperties;
    }


    /**
     * 获取stub
     * @param serverId
     * @return
     */
    public RaftServiceGrpc.RaftServiceBlockingStub getStub(String serverId){
        RaftServiceGrpc.RaftServiceBlockingStub stub = stubs.get(serverId);
        if(stub == null){
            ManagedChannel managedChannel = channels.get(serverId);
            if(managedChannel == null){
                throw new IllegalArgumentException("找不到channel,一般不会到这一步");
            }else{
                stub = RaftServiceGrpc.newBlockingStub(managedChannel).withDeadlineAfter(100, TimeUnit.MILLISECONDS);
                stubs.put(serverId, stub);
            }
        }
        return stub;
    }
    /**
     * 移除Channel
     * @param peerId
     */
    public void removeChannel(String peerId){
        ManagedChannel channel = channels.remove(peerId);
        channel.shutdown();
    }

    /**
     * 添加channel
     * @param peerId
     * @return
     */
    public Channel addChannel(PeerId peerId){
        String target = peerId.toString();
        if(channels.containsKey(target)){
            return channels.get(target);
        }
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(peerId.getEndpoint().toString());
        if (grpcProperties.isPlaintext()) {
            builder.usePlaintext();
        }
        if (grpcProperties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(grpcProperties.isKeepAliveWithoutCalls())
                    .keepAliveTime(grpcProperties.getKeepAliveTime(), TimeUnit.SECONDS)
                    .keepAliveTimeout(grpcProperties.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }
        if (grpcProperties.getMaxInboundMessageSize() > 0) {
            builder.maxInboundMessageSize(grpcProperties.getMaxInboundMessageSize());
        }
        ManagedChannel channel = builder.build();
        channels.put(target, channel);
        stubs.put(target, RaftServiceGrpc.newBlockingStub(channel));
        return channel;
    }


}
