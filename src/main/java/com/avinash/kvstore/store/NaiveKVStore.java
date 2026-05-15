package com.avinash.kvstore.store;

import com.avinash.kvstore.model.ValueWrapper;
import java.util.HashMap;
import java.util.Map;

public class NaiveKVStore implements KVStore {
    private final Map<String, ValueWrapper> store;

    public NaiveKVStore() {
        store = new HashMap<>();
    }
    public void set(String key, String value, long ttlMillis) {
        store.put(key, new ValueWrapper(value, ttlMillis));
    }

    public String get(String key) {
        ValueWrapper wrapper = store.get(key);
        if (wrapper == null || wrapper.isExpired()) {
            store.remove(key);
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

    public Map<String, ValueWrapper> getStore() {
        return store;
    }
}