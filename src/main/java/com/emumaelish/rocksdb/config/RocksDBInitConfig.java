package com.emumaelish.rocksdb.config;

import com.emumaelish.service.KVDBStore;
import org.rocksdb.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "kvdb.type" , havingValue = "2")
public class RocksDBInitConfig {

    @Value("${kvdb.path}")
    public String path;

    @Bean
    public KVDBStore kvdbStore(){
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setMaxBackgroundCompactions(4);
        options.setMaxBackgroundFlushes(2);
        options.setMaxOpenFiles(-1);
        options.setWriteBufferSize(512*1024*1024);
        options.setMaxWriteBufferNumber(5);
        options.setMinWriteBufferNumberToMerge(2);
        return new RocksDBStoreImpl(path, options);
    }
}
