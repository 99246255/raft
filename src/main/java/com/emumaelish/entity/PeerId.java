package com.emumaelish.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.emumaelish.util.Copiable;
import com.emumaelish.util.CrcUtil;
import com.emumaelish.util.Endpoint;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 节点信息,toString格式 [ip]:[port]:[idx]:[节点类型]，例如127.0.0.1:1111:0:0
 */
@Log4j2
public class PeerId implements Copiable<PeerId>, Serializable, Checksum {

    private static final long serialVersionUID = 8083529734784884641L;
    /**
     * 节点IP
     */
    private Endpoint endpoint = new Endpoint(Endpoint.IP_ANY, 0);

    /**
     * index
     */
    private int idx;

    private NodeType nodeType = NodeType.SLAVE;

    /**
     * toString缓存
     */
    private String str;

    public static final PeerId ANY_PEER = new PeerId();

    /**
     * 校验和
     */
    private long checksum;

    public PeerId() {
        super();
    }

    public static byte[] unsafeEncode(final CharSequence in) {
        final int len = in.length();
        final byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = (byte) in.charAt(i);
        }
        return out;
    }

    @Override
    public long checksum() {
        if (this.checksum == 0) {
            this.checksum = CrcUtil.crc64(unsafeEncode(toString()));
        }
        return this.checksum;
    }

    /**
     * 创建空节点
     */
    public static PeerId emptyPeer() {
        return new PeerId();
    }

    @Override
    public PeerId copy() {
        return new PeerId(this.endpoint.copy(), this.idx, this.nodeType);
    }

    /**
     * 根据ip:port:idx 格式快速生成节点
     */
    public static PeerId parsePeer(final String s) {
        final PeerId peer = new PeerId();
        if (peer.parse(s)) {
            return peer;
        }
        return null;
    }

    public PeerId(final Endpoint endpoint, final int idx, NodeType nodeType) {
        super();
        this.endpoint = endpoint;
        this.idx = idx;
        this.nodeType = nodeType;
    }

    public PeerId(final String ip, final int port) {
        this(ip, port, 0, NodeType.Consensus);
    }

    public PeerId(final String ip, final int port, final int idx, NodeType nodeType) {
        super();
        this.endpoint = new Endpoint(ip, port);
        this.idx = idx;
        this.nodeType = nodeType;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public String getIp() {
        return this.endpoint.getIp();
    }

    public int getPort() {
        return this.endpoint.getPort();
    }

    public int getIdx() {
        return this.idx;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * 判断是否是空节点
     */
    @JSONField(serialize = false)
    public boolean isEmpty() {
        return getIp().equals(Endpoint.IP_ANY) && getPort() == 0 && this.idx == 0;
    }

    @Override
    public String toString() {
        if (this.str == null) {
            this.str = new StringBuilder(this.endpoint.toString()).append(":").append(this.idx).append(":").append(nodeType.getType()).toString();
        }
        return this.str;
    }

    /**
     * 从string解析PeerId属性 {@link #toString()}
     */
    public boolean parse(final String s) {
        final String[] tmps = StringUtils.split(s, ':');
        if (tmps.length != 4) {
            return false;
        }
        try {
            final int port = Integer.parseInt(tmps[1]);
            this.endpoint = new Endpoint(tmps[0], port);
            this.idx = Integer.parseInt(tmps[2]);
            this.nodeType = NodeType.getByType(Integer.parseInt(tmps[3]));
            this.str = null;
            return true;
        } catch (final Exception e) {
            log.error("Parse peer from string failed: {}", s, e);
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.endpoint == null ? 0 : this.endpoint.hashCode());
        result = prime * result + this.idx;
        result = prime * result + this.nodeType.getType();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerId other = (PeerId) obj;
        if (this.endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else if (!this.endpoint.equals(other.endpoint)) {
            return false;
        }
        if(this.idx != other.idx){
            return false;
        }
        if(this.nodeType == null && other.nodeType == null){
            return true;
        }else if(this.nodeType != null && other.nodeType != null){
            return this.nodeType.getType() == other.nodeType.getType();
        }
        return false;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
