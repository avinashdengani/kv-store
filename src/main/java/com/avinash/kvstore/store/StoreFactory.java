package com.avinash.kvstore.store;

public class StoreFactory {
    public enum StoreType {
        NAIVE,
        SYNCHRONIZED,
        CONCURRENT
    }

    public static KVStore create(StoreType type) {
        return switch (type) {
            case NAIVE -> new NaiveKVStore();
            case SYNCHRONIZED -> new SynchronizedKVStore();
            case CONCURRENT -> new ConcurrentKVStore();
            default -> throw new IllegalArgumentException("Unknown store type: " + type);
        };
    }

}
