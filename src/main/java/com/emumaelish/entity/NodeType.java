package com.emumaelish.entity;

/**
 * @Author: enumaelish
 * @Date: 2019/5/29 09:42
 * @Description: 节点类型
 */
public enum NodeType {

    /**
     * 共识节点,参与共识
     */
    Consensus(0),

    /**
     * 从节点，只负责从主节点数据同步
     */
    SLAVE(1);
    private final int type;

    NodeType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * TODO 后续拓展修改默认的
     * 0:Consensus , 其他SLAVE
     *
     * @param type
     * @return
     */
    public static NodeType getByType(int type) {
        if (type == 0) {
            return Consensus;
        } else {
            return SLAVE;
        }
    }
}
