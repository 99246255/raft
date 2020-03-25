package com.emumaelish.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * grpc 通道配置
 */
@ConfigurationProperties("peers")
public class GrpcProperties {

    private boolean plaintext = true;

    /**
     * 默认长链接,
     */
    private boolean enableKeepAlive = true;

    /**
     * Sets whether keepalive will be performed when there are no outstanding RPC on a connection.
     * Defaults to {@code false}.
     */
    private boolean keepAliveWithoutCalls = true;

    /**
     * The default delay in seconds before we send a keepalive.
     * Defaults to {@code 180}
     */
    private long keepAliveTime = 180;

    /**
     * The default timeout in seconds for a keepalive ping request.
     * Defaults to {@code 20}
     */
    private long keepAliveTimeout = 20;
    
    /**
     * The maximum message size allowed to be received on the channel.
     */
    private int maxInboundMessageSize;

    public boolean isPlaintext() {
        return plaintext;
    }

    public void setPlaintext(boolean plaintext) {
        this.plaintext = plaintext;
    }

    public boolean isEnableKeepAlive() {
        return enableKeepAlive;
    }

    public void setEnableKeepAlive(boolean enableKeepAlive) {
        this.enableKeepAlive = enableKeepAlive;
    }

    public boolean isKeepAliveWithoutCalls() {
        return keepAliveWithoutCalls;
    }

    public void setKeepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
        this.keepAliveWithoutCalls = keepAliveWithoutCalls;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(long keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }
}
