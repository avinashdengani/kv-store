package com.avinash.kvstore;

import com.avinash.kvstore.benchmark.ConcurrencyBreaker;
import com.avinash.kvstore.benchmark.ConcurrentBenchmark;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== KV Store Benchmark ===\n");

        int threadCount = 50;
        int opsPerThread = 1000;

        ConcurrentBenchmark.runAll(threadCount, opsPerThread);

        Thread.sleep(1500);
        ConcurrencyBreaker.runAll(threadCount, opsPerThread);

    }
}
