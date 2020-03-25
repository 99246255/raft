package com.emumaelish.service;

import com.emumaelish.entity.LogEntry;
import com.emumaelish.entity.Node;

import java.util.List;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 16:44
 * @Description: 本地存储服务, 包含日志存储和配置存储
 */
public interface StoreService {

	/**
	 * 默认组raft,最初版本没有组的概念，所以对初期版本产生的数据进行兼容，使用此指定的版本，后续组不能使用此组名，防止多组公用冲突
	 */
	String DEFAULT_GROUP = "RAFT";

	/**
	 * 从文件中读取所有节点信息
	 *
	 * @return
	 */
	List<Node> getAllNode();

	/**
	 * 保存节点信息
	 *
	 * @return
	 */
	void saveRaftNode(Node node);

	/**
	 * 获取初始化时节点配置，term,index=0
	 *
	 * @param groupId
	 * @return
	 */
	LogEntry getInitConfigLog(String groupId);

	/**
	 * 获取最新日志
	 *
	 * @return
	 */
	LogEntry getLastLogEntry(String groupId);

	/**
	 * 获取最老的日志
	 *
	 * @return
	 */
	LogEntry getFirstLogEntry(String groupId);

	/**
	 * 根据logindex获取日志
	 *
	 * @param logIndex
	 * @return
	 */
	LogEntry getLogEntryByLogIndex(String groupId, Long logIndex);


	/**
	 * 节点批量添加日志-从leader处接受日志，此接口不需要从尾部进行插入
	 *
	 * @param entryList
	 */
	void addLogEntrys(String groupId, List<LogEntry> entryList);

	/**
	 * leader插入日志
	 *
	 * @param logEntry
	 * @return
	 */
	LogEntry add(String groupId, LogEntry logEntry);
}
