package com.avinash.kvstore;

import com.avinash.kvstore.store.KVStore;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        KVStore store = new KVStore();

        // Basic set and get
        store.set("name", "Avinash", -1);
        System.out.println("name: " + store.get("name")); // Avinash

        // TTL test — expires in 2 seconds
        store.set("session", "abc123", 2000);
        System.out.println("session (before expiry): " + store.get("session")); // abc123

        Thread.sleep(2500); // wait 2.5 seconds

        System.out.println("session (after expiry): " + store.get("session")); // null

        // Delete test
        store.set("temp", "xyz", -1);
        store.delete("temp");
        System.out.println("temp (after delete): " + store.get("temp")); // null

        System.out.println("Store size: " + store.size()); // 1 (only 'name' remains)
    }
}