package com.emumaelish.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: enumaelish
 * @Date: 2018/9/11 13:43
 * @Description: 附加日志请求
 */
@Data
public class AppendEntriesRequest {
    /**
     * 领导人的Id
     */
    private String serverId;
    /**
     * 领导人的任期号
     */
    private long term;
    /**
     * 新的日志条目紧随之前的索引值
     */
    private long prevLogIndex;

    /**
     * 新的日志条目紧随之前的索引值
     */
    private long prevLogTerm;
    /**
     *准备存储的日志条目（表示心跳时为空）
     */
    List<LogEntry> entries;
    /**
     * 领导人已经提交的日志的索引值
     */
    private long leaderCommit;

    private String groupId;

}
