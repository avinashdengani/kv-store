package com.avinash.kvstore.expiry;

import com.avinash.kvstore.model.ValueWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiryManager {

    private final Map<String, ValueWrapper> store;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public ExpiryManager(Map<String, ValueWrapper> store) {
        this.store = store;
    }

    public void start(long intervalMs) {
        scheduler.scheduleWithFixedDelay(
                this::evictExpiredKeys,
                intervalMs,
                intervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    private void evictExpiredKeys() {
        int beforeSize = store.size();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = store.size();
        int evicted = beforeSize - afterSize;
        if (evicted > 0) {
            System.out.println("Sweeper ran — evicted " + evicted + " expired keys. Remaining: " + afterSize);
        }
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("Expiry sweeper stopped.");
    }

}
