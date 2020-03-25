package com.emumaelish.service.impl;

import com.emumaelish.common.JsonUtil;
import com.emumaelish.entity.LogEntry;
import com.emumaelish.entity.LogEntryType;
import com.emumaelish.entity.Node;
import com.emumaelish.entity.NodeStore;
import com.emumaelish.entity.conf.ConfigurationLogEntry;
import com.emumaelish.service.StoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 16:52
 * @Description: ehache 存储逻辑实现，具体存储需要实现该类抽象方法即可
 *
 */
public abstract class AbstractStoreService implements StoreService {


//    @Autowired
//    PeersProperties peersProperties;
//
//    @Autowired(required = false)
//    GrpcServerProperties grpcServerProperties;

    abstract List<Node> getListNodes(String groupId);
    /**
     * 设置kv
     * @param key
     * @param value
     */
    abstract void setValue(String groupId, String key, String value);

    /**
     * 获取value
     * @param key
     */
    abstract String getValue(String groupId, String key);

    /**
     * raft节点配置
     */
    public static final String RAFT_CONFIG = "raft_config";

    /**
     * 日志标识
     */
    public static final String RAFT_LOG = "raft_log_";

    /**
     * 初始化节点配置日志
     */
    public static final String RAFT_INIT_CONFIGLOG = "raft_log_0";
    /**
     * 最新日志标识
     */
    public static final String RAFT_LASTLOG = "raft_lastlog";

    public static Map<String, LogEntry> lastLogMap = new ConcurrentHashMap<>();

    public static Map<String, LogEntry> firstLogMap = new ConcurrentHashMap<>();

    @Override
    public List<Node> getAllNode() {
        List<Node> listNodes = getListNodes(RAFT_CONFIG);
        if(CollectionUtils.isEmpty(listNodes)) {
            listNodes = getGroupPeerPropertoes();
            // 设置节点配置LogEntry
            for (Node node: listNodes){
                LogEntry logEntry = LogEntry.getDefaultLogEntry();
                logEntry.setType(LogEntryType.CONFIGURATION);
                ConfigurationLogEntry configurationLogEntry = new ConfigurationLogEntry();
                // 获取conf
                HashSet<String> strings = new HashSet<>();
                strings.addAll(node.getPeerMap().keySet());
                strings.addAll(node.getSlaveMap().keySet());
                final StringBuilder sb = new StringBuilder();
                int i = 0;
                final int size = strings.size();
                for (final String str : strings) {
                    sb.append(str);
                    if (i < size - 1) {
                        sb.append(",");
                    }
                    i++;
                }
                configurationLogEntry.setConf(sb.toString());
                configurationLogEntry.setOldConf("");
                logEntry.setData(JsonUtil.toJSONString(configurationLogEntry));
                setValue(node.getGroupId(), RAFT_INIT_CONFIGLOG, JsonUtil.toJSONString(logEntry));
            }
        }
        return listNodes;
    }

    protected abstract void copyLog(Node node);

    /**
     * 从GroupPeerPropertoes读取配置
     * @return
     */
    protected abstract List<Node> getGroupPeerPropertoes();

    @Override
    public void saveRaftNode(Node node) {
        if(node != null){
            NodeStore nodeStore = new NodeStore();// 过滤一些无需存储的字段
            BeanUtils.copyProperties(node, nodeStore);
            setValue(RAFT_CONFIG, node.getGroupId(), JsonUtil.toJSONString(nodeStore));
        }
    }

    @Override
    public LogEntry getLastLogEntry(String groupId) {
        if(lastLogMap.containsKey(groupId)){
            return lastLogMap.get(groupId);
        }else{
            String value = getValue(groupId, RAFT_LASTLOG);
            LogEntry logEntry;
            if(value == null){
                logEntry = getInitConfigLog(groupId);
            }else {
                logEntry = JsonUtil.toBean(value, LogEntry.class);
            }
            lastLogMap.put(groupId, logEntry);
            return logEntry;
        }
    }

    @Override
    public LogEntry getInitConfigLog(String groupId) {
        String value = getValue(groupId, RAFT_INIT_CONFIGLOG);
        LogEntry logEntry = null;
        if(value != null){
            logEntry = JsonUtil.toBean(value, LogEntry.class);
        }
        return logEntry;
    }

    @Override
    public LogEntry getFirstLogEntry(String groupId) {
        if(firstLogMap.containsKey(groupId)){
            return firstLogMap.get(groupId);
        }else{
            String value = getValue(groupId, RAFT_LOG + 1);
            LogEntry logEntry;
            if(value == null){
                logEntry = getInitConfigLog(groupId);
            }else {
                logEntry = JsonUtil.toBean(value, LogEntry.class);
            }
            firstLogMap.put(groupId, logEntry);
            return logEntry;
        }
    }

    @Override
    public LogEntry getLogEntryByLogIndex(String groupId, Long logIndex) {
        String value = getValue(groupId, RAFT_LOG + logIndex);
        if(value != null){
            return JsonUtil.toBean(value, LogEntry.class);
        }
        return null;
    }

    /**
     * 此处不进行批量插入，否则可能导致GC时间过长重选leader
     * @param entryList
     */
    @Override
    public void addLogEntrys(String groupId, List<LogEntry> entryList) {
        if(CollectionUtils.isEmpty(entryList)){
            return;
        }
        for (int i = 0;i < entryList.size();i++){
            LogEntry logEntry = entryList.get(i);
            setValue(groupId, RAFT_LOG + logEntry.getIndex(), JsonUtil.toJSONString(logEntry));
            if(i == entryList.size() - 1){
                lastLogMap.put(groupId, logEntry);
                setValue(groupId, RAFT_LASTLOG, JsonUtil.toJSONString(logEntry));
            }
        }
    }


    /**
     * 添加日志，默认日志偏移量从1开始
     * @param logEntry
     * @return
     */
    @Override
    public LogEntry add(String groupId, LogEntry logEntry) {
        setValue(groupId, RAFT_LOG + logEntry.getIndex(), JsonUtil.toJSONString(logEntry));
        lastLogMap.put(groupId, logEntry);
        setValue(groupId, RAFT_LASTLOG, JsonUtil.toJSONString(logEntry));
        return logEntry;
    }


}
