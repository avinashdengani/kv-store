package com.avinash.kvstore.store;

import com.avinash.kvstore.model.ValueWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SynchronizedKVStore implements KVStore{
    private final Map<String, ValueWrapper> kvStore;

    public SynchronizedKVStore() {
        kvStore = Collections.synchronizedMap(new HashMap<>());
    }

    public void set(String key, String value, long ttlMillis) {
        synchronized (kvStore) {
            kvStore.put(key, new ValueWrapper(value, ttlMillis));
        }
    }

    public void delete(String key) {
        synchronized (kvStore) {
            kvStore.remove(key);
        }
    }

    public int size() {
        synchronized (kvStore) {
            return kvStore.size();
        }
    }
    public String get(String key) {
        synchronized (kvStore) {
            ValueWrapper wrapper = kvStore.get(key);
            if (wrapper == null || wrapper.isExpired()) {
                kvStore.remove(key);
                return null;
            }
            return wrapper.getValue();
        }
    }

    public Map<String, ValueWrapper> getStore() {
        return kvStore;
    }
}
