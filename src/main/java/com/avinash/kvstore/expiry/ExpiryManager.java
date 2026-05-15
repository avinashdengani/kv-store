package com.avinash.kvstore.expiry;

import com.avinash.kvstore.model.ValueWrapper;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiryManager {

    // Singleton instance
    private static volatile ExpiryManager instance;
    private final EvictionPolicy evictionPolicy;
    private SweeperExceptionHandler exceptionHandler;

    private final Map<String, ValueWrapper> kvStoreMap;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private ExpiryManager(Map<String, ValueWrapper> kvStoreMap, EvictionPolicy policy) {
        this.kvStoreMap = kvStoreMap;
        this.evictionPolicy = policy;

        this.exceptionHandler = e -> System.out.println(
                "Sweeper exception: " + e.getClass().getSimpleName()
                        + " — " + e.getMessage()
        );
    }

    // Double-checked locking — thread-safe singleton
    public static ExpiryManager getInstance(Map<String, ValueWrapper> kvStoreMap) {
        return getInstance(kvStoreMap, EvictionPolicies.ttlBased()); // default
    }

    public static ExpiryManager getInstance(Map<String, ValueWrapper> kvStoreMap,
                                            EvictionPolicy policy) {
        if (instance == null) {
            synchronized (ExpiryManager.class) {
                if (instance == null) {
                    instance = new ExpiryManager(kvStoreMap, policy);
                }
            }
        }
        return instance;
    }

    // Set custom exception handler — called after getInstance
    public ExpiryManager withExceptionHandler(SweeperExceptionHandler handler) {
        this.exceptionHandler = handler;
        return this; // fluent API — allows chaining
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
        try {
            int beforeSize = kvStoreMap.size();
            kvStoreMap.entrySet().removeIf(entry -> evictionPolicy.shouldEvict(entry.getKey(), entry.getValue()));
            int afterSize = kvStoreMap.size();
            int evicted = beforeSize - afterSize;
            if (evicted > 0) {
                System.out.println("Sweeper ran — evicted " + evicted
                        + " expired keys. Remaining: " + afterSize);
            }
        } catch (Exception e) {
            this.exceptionHandler.handle(e);
        }
    }

    public void stop() {
        scheduler.shutdown();
        instance = null; // reset so new instance can be created if needed
        System.out.println("Expiry sweeper stopped.");
    }
}
