package com.emumaelish.rocksdb.config;

import com.emumaelish.service.KVDBStore;
import com.emumaelish.service.StoreService;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.rocksdb.*;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RocksDBStoreImpl implements KVDBStore {

    private String path;

    private Options options;

    Map<String, RocksDB> dbMap = new ConcurrentHashMap<String, RocksDB>();

    public RocksDBStoreImpl(String path, Options options) {
        this.path = path;
        this.options = options;
    }
    private String getPath(String groupId){
        return path + File.separator + groupId;
    }
    private RocksDB getDB(String groupId){
        if (groupId == null) {
            groupId = StoreService.DEFAULT_GROUP;
        }
        if(dbMap.containsKey(groupId)){
            return dbMap.get(groupId);
        }else {
            String u_path = getPath(groupId);
            File file = new File(u_path);
            if(!file.exists()){
                file.mkdirs();
            }
            RocksDB db = null;
            try {
                db = RocksDB.open(options, u_path);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
            dbMap.put(groupId, db);
            return db;
        }
    }

    @Override
    public void put(String key, String value, String groupId) {
        RocksDB db = getDB(groupId);
        try {
            db.put(Iq80DBFactory.bytes(key), Iq80DBFactory.bytes(value));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void batchPut(Map<String, String> map, String groupId) {
        if(CollectionUtils.isEmpty(map)){
            return;
        }
        RocksDB db = getDB(groupId);
        try (final WriteOptions writeOpt = new WriteOptions()) {
            try (final WriteBatch batch = new WriteBatch()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    batch.put(Iq80DBFactory.bytes(entry.getKey()), Iq80DBFactory.bytes(entry.getValue()));
                }
                db.write(writeOpt, batch);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(String key, String groupId) {
        RocksDB db = getDB(groupId);
        try {
            return Iq80DBFactory.asString(db.get(Iq80DBFactory.bytes(key)));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void remove(String key, String groupId) {
        RocksDB db = getDB(groupId);
        try {
            db.delete(Iq80DBFactory.bytes(key));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

}
