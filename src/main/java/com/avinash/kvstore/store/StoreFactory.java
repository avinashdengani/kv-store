package com.avinash.kvstore.store;

public class StoreFactory {
    public enum StoreType {
        NAIVE,
        SYNCHRONIZED,
        CONCURRENT,
        READ_WRITE
    }

    public static KVStore create(StoreType type) {
        return switch (type) {
            case NAIVE -> new NaiveKVStore();
            case SYNCHRONIZED -> new SynchronizedKVStore();
            case CONCURRENT -> new ConcurrentKVStore();
            case READ_WRITE -> new ReadWriteKVStore();
                default -> throw new IllegalArgumentException("Unknown store type: " + type);
        };
    }

}
