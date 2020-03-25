package com.emumaelish.service;

import com.emumaelish.entity.PeerId;
import com.emumaelish.entity.PeerInfo;
import com.emumaelish.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: enumaelish
 * @Date: 2019/5/28 08:46
 * @Description:
 */
@Service
public class RaftNodeService {


    @Autowired
    StoreService storeService;

    /**
     * 各个组节点情况
     */
    private Map<String, Node> groupNodes = new ConcurrentHashMap<>();

    @Autowired
    RpcRequestService rpcRequestService;
    /**
     * 初始化groupNodes信息
     */
    public void init(){
        List<Node> allNode = storeService.getAllNode();
        for (final Node node: allNode){
            groupNodes.put(node.getGroupId(), node);
        }
        // 初始化所有节点通道
        Set<PeerId> peerSet = getPeerSet();
        for(PeerId peer: peerSet){
            rpcRequestService.addChannel(peer);
        }

    }
    /**
     * 获取所有节点，用于初始化channel
     * @return
     */
    public Set<PeerId> getPeerSet(){
        Set<PeerId> set = new HashSet<PeerId>();
        for (Node node : groupNodes.values()){
            Collection<PeerInfo> values = node.getPeerMap().values();
            for(PeerInfo peer: values){
                set.add(peer.getPeerId());
            }
            values = node.getSlaveMap().values();
            for(PeerInfo peer: values){
                set.add(peer.getPeerId());
            }
        }
        return set;
    }

    public Node getNodeByGroupId(String string){
        return groupNodes.get(string);
    }

    public Collection<Node> getAllNodes(){
        return groupNodes.values();
    }

    /**
     * 获取默认节点
     * @return
     */
    public Node getDefaultNode(){
        return groupNodes.get(StoreService.DEFAULT_GROUP);
    }
}
