package com.emumaelish.entity;

import lombok.Data;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: enumaelish
 * @Date: 2019/6/3 16:31
 * @Description:
 */
@Data
public class Ballot {

    // 在当前获得选票的候选人的Id
    private PeerId votedFor;

    // 投票数
    private Integer voteCount = 0;

    private Lock lock = new ReentrantLock();

}
