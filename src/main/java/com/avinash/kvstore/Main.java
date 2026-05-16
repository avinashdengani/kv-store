package com.avinash.kvstore;

import com.avinash.kvstore.benchmark.BenchmarkResult;
import com.avinash.kvstore.benchmark.ConcurrencyBreaker;
import com.avinash.kvstore.benchmark.ConcurrentBenchmark;

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== KV Store Benchmark ===\n");

        int threadCount = 50;
        int opsPerThread = 1000;

        List<BenchmarkResult> results = ConcurrentBenchmark.runAll(threadCount, opsPerThread);
        BenchmarkResult.printSummary(results);

        Thread.sleep(1500);
        ConcurrencyBreaker.runAll(threadCount, opsPerThread);

    }
}
