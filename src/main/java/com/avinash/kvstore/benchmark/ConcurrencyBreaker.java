package com.avinash.kvstore.benchmark;

import com.avinash.kvstore.expiry.ExpiryManager;
import com.avinash.kvstore.store.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyBreaker {

    public static List<BreakerResult> runAll(int threadCount, int keysPerThread) throws InterruptedException {
        System.out.println("=== Concurrency Breaker ===\n");
        System.out.println("Strategy: Concurrent writes + ExpiryManager sweeping simultaneously");
        System.out.println("Each thread writes UNIQUE keys with short TTL");
        System.out.println("Sweeper evicts while writers are still inserting\n");
        System.out.println("Running 5 trials per implementation...\n");

        List<BreakerResult> results = new ArrayList<>();

        for (StoreFactory.StoreType type : StoreFactory.StoreType.values()) {
            System.out.println("--- " + type.name() + " ---");
            results.add(runTrials(type.name(),
                    type, 5, threadCount, keysPerThread));
            System.out.println();
        }
        return results;
    }

    private static BreakerResult runTrials(String label,
                                           StoreFactory.StoreType type,
                                           int trials,
                                           int threadCount,
                                           int keysPerThread)
            throws InterruptedException {

        int totalCorrupted       = 0;
        long totalLost           = 0;
        int totalWriterExc       = 0;
        int totalSweeperExc      = 0;

        for (int i = 0; i < trials; i++) {
            TrialResult trial = runBreaker(
                    "Trial " + (i + 1),
                    StoreFactory.create(type),
                    threadCount,
                    keysPerThread
            );

            if (trial.corrupted) {
                totalCorrupted++;
            }

            totalLost       += trial.keysLost;
            totalWriterExc  += trial.writerExceptions;
            totalSweeperExc += trial.sweeperExceptions;
        }

        return new BreakerResult(label, trials, totalCorrupted,
                totalLost, totalWriterExc, totalSweeperExc);
    }

    // Internal result per trial — not exposed outside
    private static class TrialResult {
        final boolean corrupted;
        final long keysLost;
        final int writerExceptions;
        final int sweeperExceptions;

        TrialResult(boolean corrupted, long keysLost,
                    int writerExceptions, int sweeperExceptions) {
            this.corrupted         = corrupted;
            this.keysLost          = keysLost;
            this.writerExceptions  = writerExceptions;
            this.sweeperExceptions = sweeperExceptions;
        }
    }

    private static TrialResult runBreaker(String label, KVStore kvStore,
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

        int actualSize   = kvStore.size();
        long keysLost    = expectedSize - actualSize;
        boolean corrupted = actualSize != expectedSize
                || exceptionCount.get() > 0
                || sweeperExceptions.get() > 0;

        return new TrialResult(corrupted, keysLost,
                exceptionCount.get(), sweeperExceptions.get());
    }
}