package com.avinash.kvstore.benchmark;


import com.avinash.kvstore.store.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBenchmark {

    public static List<BenchmarkResult> runAll(int threadCount,
                                                         int opsPerThread) {

        List<BenchmarkResult> results = new ArrayList<>();

        StoreFactory.StoreType[] storeTypes = StoreFactory.StoreType.values();

        for (StoreFactory.StoreType storeType : storeTypes) {

            try {

                results.add(
                        runBenchmark(
                                storeType.name(),
                                StoreFactory.create(storeType),
                                threadCount,
                                opsPerThread
                        )
                );

                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("Benchmark failed for " + storeType.name()
                        + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return results;
    }

    public static BenchmarkResult runBenchmark(String label, KVStore kvStore,
                                    int threadCount, int opsPerThread)
            throws InterruptedException {

        // Pre-populate — MUST happen before threads start
        for (int i = 0; i < 10; i++) {
            kvStore.set("key" + i, "value" + i, -1);
        }

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

        long elapsed    = System.currentTimeMillis() - start;
        long totalOps   = (long) threadCount * opsPerThread;
        long throughput = totalOps * 1000L / elapsed;
        int finalSize  = kvStore.size();
        boolean corrupted = finalSize != 10 || unexpectedNulls.get() > 0;

        // Return result object
        return new BenchmarkResult(
                label, throughput, unexpectedNulls.get(),
                corrupted, elapsed, finalSize
        );
    }}
