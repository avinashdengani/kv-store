package com.avinash.kvstore.benchmark;


import com.avinash.kvstore.store.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBenchmark {

    public static void runAll(int threadCount, int opsPerThread) throws InterruptedException {

        runBenchmark(StoreFactory.StoreType.CONCURRENT.name(), StoreFactory.create(StoreFactory.StoreType.CONCURRENT), threadCount, opsPerThread);
        Thread.sleep(500);

        runBenchmark(StoreFactory.StoreType.SYNCHRONIZED.name(), StoreFactory.create(StoreFactory.StoreType.SYNCHRONIZED), threadCount, opsPerThread);
        Thread.sleep(500);

        runBenchmark(StoreFactory.StoreType.NAIVE.name(), StoreFactory.create(StoreFactory.StoreType.NAIVE), threadCount, opsPerThread);
        Thread.sleep(500);
    }

    private static void runBenchmark(String label, KVStore kvStore,
                                    int threadCount, int opsPerThread)
            throws InterruptedException {

        // Internal warmup for this specific store
        for (int i = 0; i < 10; i++) {
            kvStore.set("key" + i, "value" + i, -1);
        }
        // Small warmup run
        ExecutorService warmupExecutor = Executors.newFixedThreadPool(10);
        CountDownLatch warmupLatch = new CountDownLatch(10);
        for (int t = 0; t < 10; t++) {
            warmupExecutor.submit(() -> {
                for (int i = 0; i < 1000; i++) {
                    kvStore.get("key" + (i % 100));
                }
                warmupLatch.countDown();
            });
        }
        warmupLatch.await();
        warmupExecutor.shutdown();

        // Now actual benchmark
        AtomicInteger successfulReads = new AtomicInteger(0);
        AtomicInteger successfulWrites = new AtomicInteger(0);
        AtomicInteger unexpectedNulls = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        String key = "key" + (i % 10);
                        if (i % 10 < 3) {
                            String val = kvStore.get(key);
                            if (val != null) {
                                successfulReads.incrementAndGet();
                            } else {
                                unexpectedNulls.incrementAndGet(); // key existed but returned null
                            }
                        } else {
                            kvStore.set(key, "val_" + threadId, -1);
                            successfulWrites.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long elapsed = System.currentTimeMillis() - start;
        long totalOps = (long) threadCount * opsPerThread;
        long throughput = totalOps * 1000L / elapsed;

        System.out.println("--- " + label + " ---");
        System.out.println("Threads      : " + threadCount);
        System.out.println("Total ops    : " + totalOps);
        System.out.println("Reads        : " + successfulReads.get());
        System.out.println("Writes       : " + successfulWrites.get());
        System.out.println("Time         : " + elapsed + "ms");
        System.out.println("Throughput   : " + throughput + " ops/sec");

        // Validation — catch silent corruption
        int finalSize = kvStore.size();
        System.out.println("Final store size : " + finalSize);
        System.out.println("Expected max size: 10");
        System.out.println("Corruption check : " + (finalSize == 10 ? "✓ CLEAN" : "✗ CORRUPTED — data lost silently"));
        System.out.println("Unexpected nulls : " + unexpectedNulls.get() +
                (unexpectedNulls.get() > 0 ? " ✗ CORRUPTION DETECTED" : " ✓ NONE"));
        System.out.println();

        // Sample value check
        System.out.println("Sample reads after benchmark:");
        for (int i = 0; i < 5; i++) {
            String val = kvStore.get("key" + i);
            System.out.println("  key" + i + " → " + (val != null ? val : "NULL (missing!)"));
        }
        System.out.println();
    }}
