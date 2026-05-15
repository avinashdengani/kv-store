package com.avinash.kvstore.benchmark;

import com.avinash.kvstore.expiry.ExpiryManager;
import com.avinash.kvstore.store.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyBreaker {

    public static void runAll(int threadCount, int keysPerThread) throws InterruptedException {
        System.out.println("=== Concurrency Breaker ===\n");
        System.out.println("Strategy: Concurrent writes + ExpiryManager sweeping simultaneously");
        System.out.println("Each thread writes UNIQUE keys with short TTL");
        System.out.println("Sweeper evicts while writers are still inserting\n");
        System.out.println("Running 5 trials per implementation...\n");

        System.out.println("--- Naive HashMap ---");
        for (int i = 0; i < 5; i++) {
            runBreaker("Trial " + (i + 1), StoreFactory.create(StoreFactory.StoreType.NAIVE), threadCount, keysPerThread);
        }

        System.out.println("\n--- Synchronized HashMap ---");
        for (int i = 0; i < 5; i++) {
            runBreaker("Trial " + (i + 1), StoreFactory.create(StoreFactory.StoreType.SYNCHRONIZED), threadCount, keysPerThread);
        }

        System.out.println("\n--- ConcurrentHashMap ---");
        for (int i = 0; i < 5; i++) {
            runBreaker("Trial " + (i + 1), StoreFactory.create(StoreFactory.StoreType.CONCURRENT), threadCount, keysPerThread);
        }
    }

    public static void runBreaker(String label, KVStore kvStore,
                                  int threadCount, int keysPerThread)
            throws InterruptedException {

        int expectedSize = threadCount * keysPerThread;
        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger sweeperExceptions = new AtomicInteger(0);

        // Start ExpiryManager — sweeps every 50ms aggressively
        ExpiryManager expiryManager = ExpiryManager.getInstance(kvStore.getStore())
                .withExceptionHandler(e -> sweeperExceptions.incrementAndGet());
        expiryManager.start(50);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < keysPerThread; i++) {
                        String key = "key_" + threadId + "_" + i;
                        try {
                            // Short TTL — sweeper will try to evict while
                            // other threads are still writing
                            kvStore.set(key, "value_" + threadId, 3000);
                        } catch (Exception e) {
                            exceptionCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Stop sweeper
        expiryManager.stop();

        int actualSize = kvStore.size();
        boolean corrupted = actualSize != expectedSize;
        boolean hadExceptions = exceptionCount.get() > 0;
        boolean sweeperFailed = sweeperExceptions.get() > 0;

        System.out.printf(
                "  %s | Expected: %d | Actual: %d | Lost: %d | " +
                        "Writer Exceptions: %d | Sweeper Exceptions: %d | %s%n",
                label,
                expectedSize,
                actualSize,
                expectedSize - actualSize,
                exceptionCount.get(),
                sweeperExceptions.get(),
                (corrupted || hadExceptions || sweeperFailed)
                        ? "✗ CORRUPTED" : "✓ CLEAN"
        );
    }
}