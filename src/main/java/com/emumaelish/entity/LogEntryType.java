package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2018/9/7 15:05
 * @Description: 日志类型
 */
public enum LogEntryType {
    /**
     * K,V数据
     */
    DATA(0),
    /**
     * 配置,对应结构体ConfigurationLogEntry
     */
    CONFIGURATION(1);

    private final int type;

    LogEntryType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static LogEntryType getEntryType(int type){
        if(type == 0){
            return LogEntryType.DATA;
        }else if(type == 1){
            return LogEntryType.CONFIGURATION;
        }else{
            throw new IllegalArgumentException("找不到对应的EntryType");
        }
    }
}
