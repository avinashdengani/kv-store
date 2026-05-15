package com.avinash.kvstore.store;

import com.avinash.kvstore.model.ValueWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KVStore {
    private final Map<String, ValueWrapper> store = new ConcurrentHashMap<>();

    public void set(String key, String value, long ttlMillis) {
        store.put(key, new ValueWrapper(value, ttlMillis));
    }

    public String get(String key) {
        ValueWrapper wrapper = store.get(key);
        if (wrapper == null || wrapper.isExpired()) {
            store.remove(key); // lazy eviction
            return null;
        }
        return wrapper.getValue();
    }

    public void delete(String key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }

    // Expose store for ExpiryManager
    public Map<String, ValueWrapper> getStore() {
        return store;
    }
}
