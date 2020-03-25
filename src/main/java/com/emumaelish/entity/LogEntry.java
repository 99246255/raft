package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2018/9/7 15:04
 * @Description: 日志格式, 暂时不添加groupid,因为在存储上已经有了
 */
public class LogEntry {
    /**
     * 日志所属term
     */
    private long term;
    /**
     * 日志偏移量
     */
    private long index;

    /**
     * 日志类型
     */
    private LogEntryType type;
    /**
     * 日志数据
     */
    private String data;

    /**
     * 获取默认日志，初始化没日志记录时使用
     * @return
     */
    public static LogEntry getDefaultLogEntry(){
        LogEntry logEntry = new LogEntry();
        logEntry.setIndex(0L);
        logEntry.setTerm(0L);
        return logEntry;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public LogEntryType getType() {
        return type;
    }

    public void setType(LogEntryType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
