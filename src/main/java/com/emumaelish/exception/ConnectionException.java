package com.emumaelish.exception;

/**
 * @Author: enumaelish
 * @Date: 2018/9/19 14:47
 * @Description: rpc连接异常
 */
public class ConnectionException extends  RuntimeException{

    public ConnectionException(String message) {
        super(message);
    }
}
