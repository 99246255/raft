package com.emumaelish.entity.conf;

import com.emumaelish.entity.PeerId;

import java.util.HashSet;
import java.util.Set;

/**
 * 节点配置日志
 */
public class ConfigurationEntry {

    /**
     * 日志所属term
     */
    private long term;
    /**
     * 日志偏移量
     */
    private long index;

    /**
     * 新日志配置
     */
    private Configuration conf = new Configuration();
    /**
     * 老日志配置
     */
    private Configuration oldConf = new Configuration();

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getOldConf() {
        return this.oldConf;
    }

    public void setOldConf(Configuration oldConf) {
        this.oldConf = oldConf;
    }

    public ConfigurationEntry() {
        super();
    }

    public ConfigurationEntry(long term, long index, Configuration conf, Configuration oldConf) {
        this.term = term;
        this.index = index;
        this.conf = conf;
        this.oldConf = oldConf;
    }

    public boolean isStable() {
        return this.oldConf.isEmpty();
    }

    public boolean isEmpty() {
        return this.conf.isEmpty();
    }

    public Set<PeerId> listPeers() {
        final Set<PeerId> ret = new HashSet<>(this.conf.listPeers());
        ret.addAll(this.oldConf.listPeers());
        return ret;
    }

    public boolean contains(PeerId peer) {
        return this.conf.contains(peer) || this.oldConf.contains(peer);
    }


}
