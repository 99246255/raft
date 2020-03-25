package com.emumaelish.service.impl;


import com.emumaelish.common.JsonUtil;
import com.emumaelish.config.RaftOptions;
import com.emumaelish.entity.Node;
import com.emumaelish.entity.PeerId;
import com.emumaelish.entity.PeerInfo;
import com.emumaelish.service.StoreService;
import com.emumaelish.config.GroupPeerPropertoes;
import com.emumaelish.entity.NodeType;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 16:52
 * @Description: rocksdb 存储实现
 *
 */
@Service
public class RocksDbServiceImpl extends AbstractStoreService {

    @Autowired
    RaftOptions raftOptions;

    @Autowired
    GroupPeerPropertoes groupPeerPropertoes;

    @Value("${grpc.server.port}")
    String port;

    @Value("${grpc.server.address}")
    String address;

    private Map<String, RocksDB> dbMap = new ConcurrentHashMap<String, RocksDB>();

    private Options options;
    @PostConstruct
    public void init() {
        options = new Options();
        options.setCreateIfMissing(true);
        options.setMaxBackgroundCompactions(4);
        options.setMaxBackgroundFlushes(2);
        options.setWriteBufferSize(512*1024*1024);
        options.setMaxOpenFiles(-1);
        options.setMaxWriteBufferNumber(5);
        options.setMinWriteBufferNumberToMerge(2);
    }

    /**
     * groupId为空时，直接取配置目录的rocksdb，1.0.0版本之前的旧数据存储在此
     * @param groupId
     * @return
     */
    public RocksDB getDB(String groupId){
        if (StringUtils.isEmpty(groupId)) {
            groupId = "";
        }
        if(dbMap.containsKey(groupId)){
            return dbMap.get(groupId);
        }else {
            String u_path;
            if (StringUtils.isEmpty(groupId)) {
                u_path = raftOptions.getDataDir();
            }else{
                u_path = raftOptions.getDataDir() + File.separator + groupId;
            }
            File file = new File(u_path);
            if(!file.exists()){
                file.mkdirs();
            }
            RocksDB db = null;
            try {
                db = RocksDB.open(options, u_path);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
            dbMap.put(groupId, db);
            return db;
        }
    }

    @Override
    List<Node> getListNodes(String groupId) {
        RocksDB db = getDB(groupId);
        RocksIterator iter = db.newIterator();
        List<Node> list = new ArrayList<>();
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            String string = Iq80DBFactory.asString(iter.value());
            if(StringUtils.isEmpty(string)){
                continue;
            }
            try {
                Node node = JsonUtil.toBean(string, Node.class);
                list.add(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    void setValue(String groupId, String key, String value) {
        try {
            getDB(groupId).put(Iq80DBFactory.bytes(key), Iq80DBFactory.bytes(value));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    String getValue(String groupId, String key) {
        try {
            return Iq80DBFactory.asString( getDB(groupId).get(Iq80DBFactory.bytes(key)));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void copyLog(Node node) {
        RocksDB olddb = getDB(null);// 取旧数据
        RocksDB db = getDB(StoreService.DEFAULT_GROUP);
        byte[] key;
        for (long i = 1; i <= node.getCommitIndex(); i++){
            try {
                key = Iq80DBFactory.bytes(RAFT_LOG + i);
                db.put(key, olddb.get(key));// 存放新数据
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
        try {
            key = Iq80DBFactory.bytes(RAFT_LASTLOG);
            db.put(key, olddb.get(key));// 存放最新日志
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Node> getGroupPeerPropertoes() {
        List<Node> list = new ArrayList<>();
        Map<String, List<String>> groups = groupPeerPropertoes.getGroups();
        if(groups == null && groups.size() == 0){
            throw new IllegalArgumentException("请配置节点信息");
        }
        for (Map.Entry<String, List<String>> entry: groups.entrySet()){
            String key = entry.getKey();
            Node node = new Node();
            if(StringUtils.isEmpty(key)){
                throw new IllegalArgumentException("groupId 不可为空");
            }
            node.setGroupId(key);
            PeerId peerId = new PeerId(address, Integer.parseInt(port), 0, NodeType.Consensus);
            node.setPeerId(peerId);
            List<String> value = entry.getValue();
            Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>();
            Map<String, PeerInfo> slaveMap = new ConcurrentHashMap<>();
            for (String str : value){
                PeerId peer = PeerId.parsePeer(str);
                if(peer == null){
                    throw new IllegalArgumentException("节点配置错误，节点格式为[ip]:[port]:[idx]:[节点类型]");
                }
                PeerInfo peerInfo = new PeerInfo(peer);
                if(NodeType.Consensus.equals(peer.getNodeType())){
                    peerMap.put(peer.toString(), peerInfo);
                }else{
                    slaveMap.put(peer.toString(), peerInfo);
                }
            }
            node.setPeerMap(peerMap);
            node.setSlaveMap(slaveMap);
            list.add(node);
        }
        for (Node node: list){
            saveRaftNode(node);
        }
        return list;
    }

}
