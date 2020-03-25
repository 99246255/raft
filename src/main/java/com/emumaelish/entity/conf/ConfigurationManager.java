package com.emumaelish.entity.conf;

import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 节点配置管理，因为节点变化不是很多情况，可以将历史的变化也放内存
 */
@Log4j2
public class ConfigurationManager {

    private final LinkedList<ConfigurationEntry> configurations = new LinkedList<>();
    private ConfigurationEntry snapshot = new ConfigurationEntry();

    /**
     * 添加新配置
     */
    public boolean add(ConfigurationEntry entry) {
        if (!this.configurations.isEmpty()) {
            if (this.configurations.peekLast().getIndex() >= entry.getIndex()) {
                log.error("Did you forget to call truncateSuffix before the last log index goes back.");
                return false;
            }
        }
        return this.configurations.add(entry);
    }

    /**
     * [1, first_index_kept) are being discarded
     */
    public void truncatePrefix(long firstIndexKept) {
        while (!this.configurations.isEmpty() && this.configurations.peekFirst().getIndex() < firstIndexKept) {
            this.configurations.pollFirst();
        }
    }

    /**
     * (last_index_kept, infinity) are being discarded
     */
    public void truncateSuffix(long lastIndexKept) {
        while (!this.configurations.isEmpty() && this.configurations.peekLast().getIndex() > lastIndexKept) {
            this.configurations.pollLast();
        }
    }

    public ConfigurationEntry getSnapshot() {
        return this.snapshot;
    }

    public void setSnapshot(ConfigurationEntry snapshot) {
        this.snapshot = snapshot;
    }

    public ConfigurationEntry getLastConfiguration() {
        if (this.configurations.isEmpty()) {
            return snapshot;
        } else {
            return this.configurations.peekLast();
        }
    }

    public ConfigurationEntry get(long lastIncludedIndex) {
        if (this.configurations.isEmpty()) {
            return snapshot;
        }
        ListIterator<ConfigurationEntry> it = this.configurations.listIterator();
        while (it.hasNext()) {
            if (it.next().getIndex() > lastIncludedIndex) {
                it.previous();
                break;
            }
        }
        if (it.hasPrevious()) {
            // find the first position that is less than or equal to lastIncludedIndex.
            return it.previous();
        } else {
            // position not found position, return snapshot.
            return snapshot;
        }
    }
}
