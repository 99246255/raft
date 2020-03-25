package com.emumaelish.service;

import com.emumaelish.entity.LogEntry;
import com.emumaelish.entity.Node;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 9:58
 * @Description: Raft状态机接口类
 */
public interface StateMachine {

    /**
     * 对状态机中数据进行快照，每个节点本地定时调用
     * @param snapshotDir 快照数据输出目录
     */
    void writeToSnapshot(String snapshotDir);

    /**
     * 读取快照信息到状态机，节点启动时调用
     * @param snapshotDir 快照数据目录
     */
    void readFromSnapshot(String snapshotDir);

    /**
     * 更新状态机
     * @param logEntry
     */
    void updateToStateMachine(Node node, LogEntry logEntry);


    /**
     * 根据交易偏移量获取交易hash
     * @return
     */
    String getKey(String groupId, String key);


}
