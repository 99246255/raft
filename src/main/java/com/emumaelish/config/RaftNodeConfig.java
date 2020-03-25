package com.emumaelish.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: enumaelish
 * @Date: 2018/9/6 16:14
 * @Description: 初始化节点信息
 */
@Configuration
public class RaftNodeConfig {

    /**
     * 此初始化是组节点配置
     * @return
     */
    @Bean
    public GroupPeerPropertoes groupPeerPropertoes() {
        return new GroupPeerPropertoes();
    }

    @Bean
    public RaftOptions raftOptions(){
        return new RaftOptions();
    }


}
