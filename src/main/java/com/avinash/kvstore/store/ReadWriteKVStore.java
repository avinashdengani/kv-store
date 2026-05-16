package com.avinash.kvstore.store;

import com.avinash.kvstore.model.ValueWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteKVStore implements KVStore {
    private final Map<String, ValueWrapper> kvStore;
    private final ReadWriteLock lock;

    public ReadWriteKVStore() {
        this.kvStore = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void set(String key, String value, long ttlMillis) {
        lock.writeLock().lock();
        try {
            kvStore.put(key, new ValueWrapper(value, ttlMillis));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(String key) {
        lock.readLock().lock();
        try {
            ValueWrapper wrapper = kvStore.get(key);
            if (wrapper == null || wrapper.isExpired()) return null;
            return wrapper.getValue();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void delete(String key) {
        lock.writeLock().lock();
        try { kvStore.remove(key); }
        finally { lock.writeLock().unlock(); }
    }

    public int size() {
        lock.readLock().lock();
        try { return kvStore.size(); }
        finally { lock.readLock().unlock(); }
    }

    public Map<String, ValueWrapper> getStore() { return kvStore; }
}