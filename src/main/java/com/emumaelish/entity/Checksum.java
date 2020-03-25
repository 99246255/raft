package com.emumaelish.entity;

/**
 * 校验实体
 */
public interface Checksum {

    /**
     * 计算校验值
     */
    long checksum();

    /**
     * 比较校验值
     */
    default long checksum(final long v1, final long v2) {
        return v1 ^ v2;
    }
}
