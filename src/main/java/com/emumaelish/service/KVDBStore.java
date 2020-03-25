package com.emumaelish.service;

import java.util.Map;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 9:58
 * @Description: kv存储接口,状态机状态
 */
public interface KVDBStore {

    /**
     * 设置key value
     *
     * @param key
     *         key
     * @param value
     *         value
     */
    void put(String key, String value, String groupId);

    /**
     * 批量设置
     * @param map
     */
    void batchPut(Map<String, String> map, String groupId);

    /**
     * 根据 Key获取value
     *
     * @param key
     *         key
     * @return value
     */
    String get(String key, String groupId);

    /**
     * 根据key移除value
     *
     * @param key
     *         key
     */
    void remove(String key, String groupId);
}
