package com.emumaelish.entity.conf;

import lombok.Data;

/**
 * 节点配置日志
 */
@Data
public class ConfigurationLogEntry {

    /**
     * 新日志配置
     */
    private String conf;
    /**
     * 老日志配置
     */
    private String oldConf;

}
