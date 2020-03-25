package com.emumaelish.entity;

import com.emumaelish.common.JsonUtil;

/**
 * @Author: enumaelish
 * @Date: 2018/9/12 17:43
 * @Description: 状态机数据
 */
public class KVEntity {

    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return JsonUtil.toJSONString(this);
    }
}
