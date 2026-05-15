package com.avinash.kvstore.store;

import com.avinash.kvstore.model.ValueWrapper;

import java.util.Map;

public interface KVStore {
    void set(String key, String value, long ttlMillis);
    String get(String key);
    void delete(String key);
    int size();
    Map<String, ValueWrapper> getStore();
}
