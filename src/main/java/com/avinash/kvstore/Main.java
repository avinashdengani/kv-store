package com.avinash.kvstore;

import com.avinash.kvstore.expiry.ExpiryManager;
import com.avinash.kvstore.store.KVStore;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        KVStore kvStore = new KVStore();
        ExpiryManager expiryManager = new ExpiryManager(kvStore.getStore());

        // Start sweeper — runs every 1 second
        expiryManager.start(1000);

        // Add keys with TTL
        kvStore.set("key1", "value1", 2000);  // expires in 2s
        kvStore.set("key2", "value2", 3000);  // expires in 3s
        kvStore.set("key3", "value3", 5000);  // expires in 5s
        kvStore.set("permanent", "stays", -1); // no expiry

        System.out.println("Initial size: " + kvStore.size());

        Thread.sleep(2500);
        System.out.println("After 2.5s — size: " + kvStore.size()); // 3

        Thread.sleep(1000);
        System.out.println("After 3.5s — size: " + kvStore.size()); // 2

        Thread.sleep(2000);
        System.out.println("After 5.5s — size: " + kvStore.size()); // 1 (only permanent)

        System.out.println("Permanent key: " + kvStore.get("permanent")); // stays

        expiryManager.stop();
    }
}
