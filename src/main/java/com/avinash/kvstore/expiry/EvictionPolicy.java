package com.avinash.kvstore.expiry;

import com.avinash.kvstore.model.ValueWrapper;

@FunctionalInterface
public interface EvictionPolicy {
    boolean shouldEvict(String key, ValueWrapper value);
}
