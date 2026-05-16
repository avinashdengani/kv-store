package com.avinash.kvstore;

import com.avinash.kvstore.benchmark.BenchmarkResult;
import com.avinash.kvstore.benchmark.BreakerResult;
import com.avinash.kvstore.benchmark.ConcurrencyBreaker;
import com.avinash.kvstore.benchmark.ConcurrentBenchmark;

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== KV Store Benchmark ===\n");

        int threadCount = 50;
        int opsPerThread = 1000;

        List<BenchmarkResult> benchmarkResults = ConcurrentBenchmark.runAll(threadCount, opsPerThread);
        BenchmarkResult.printSummary(benchmarkResults);

        Thread.sleep(1500);
        List<BreakerResult> breakerResults =ConcurrencyBreaker.runAll(threadCount, opsPerThread);
        BreakerResult.printSummary(breakerResults);

    }
}
