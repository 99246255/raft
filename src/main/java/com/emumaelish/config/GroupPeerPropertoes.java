package com.emumaelish.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: enumaelish
 * @Date: 2019/5/29 09:16
 * @Description:  初始化组节点配置，只在初始化是使用，后续通过节点加减来变更
 */
@ConfigurationProperties("peer")
public class GroupPeerPropertoes {

    private Map<String, List<String>> groups = new HashMap<String, List<String>>();

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }
}
