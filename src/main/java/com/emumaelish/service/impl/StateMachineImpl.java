package com.emumaelish.service.impl;

import com.emumaelish.common.JsonUtil;
import com.emumaelish.entity.*;
import com.emumaelish.service.KVDBStore;
import com.emumaelish.service.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @Author: enumaelish
 * @Date: 2018/9/10 14:48
 * @Description: 状态机实现类
 */
@Service
public class StateMachineImpl implements StateMachine {

    @Autowired
    KVDBStore kvdbStore;


    @Override
    public void writeToSnapshot(String snapshotDir) {

    }

    @Override
    public void readFromSnapshot(String snapshotDir) {

    }

    @Override
    public void updateToStateMachine(Node node, LogEntry logEntry) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        if (LogEntryType.DATA.equals(logEntry.getType())) {
            String data = logEntry.getData();
            KVEntity KVEntity = JsonUtil.toBean(data, KVEntity.class);
            if (KVEntity != null) {
                linkedHashMap.put(KVEntity.getKey(), KVEntity.getValue());
            } else {
                throw new IllegalArgumentException("日志格式有误");
            }
        } else if (LogEntryType.CONFIGURATION.equals(logEntry.getType())) {
            // 配置不会影响状态机
        }
        kvdbStore.batchPut(linkedHashMap, node.getGroupId());
    }

    @Override
    public String getKey(String groupId, String key) {
        return kvdbStore.get(key, groupId);
    }

}
