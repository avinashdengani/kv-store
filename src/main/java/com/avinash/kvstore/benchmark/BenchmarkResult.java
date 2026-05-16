package com.avinash.kvstore.benchmark;

import java.util.List;

public class BenchmarkResult {
    private final String label;
    private final long throughput;
    private final int unexpectedNulls;
    private final boolean corrupted;
    private final long timeTaken;
    private final int finalSize;

    public BenchmarkResult(String label, long throughput, int unexpectedNulls, boolean corrupted, long timeTaken, int finalSize) {
        this.label = label;
        this.throughput = throughput;
        this.unexpectedNulls = unexpectedNulls;
        this.corrupted = corrupted;
        this.timeTaken = timeTaken;
        this.finalSize = finalSize;
    }

    public String getLabel() {
        return label;
    }

    public long getThroughput() {
        return throughput;
    }

    public int getUnexpectedNulls() {
        return unexpectedNulls;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public int getFinalSize() {
        return finalSize;
    }

    public static void printSummary(List<BenchmarkResult> results) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println(  "║                     BENCHMARK SUMMARY                           ║");
        System.out.println(  "╚══════════════════════════════════════════════════════════════════╝");
        System.out.printf("%-25s %15s %10s %15s %10s%n",
                "Implementation", "Throughput/sec", "Time(ms)", "Unexpected Nulls", "Status");
        System.out.println("─".repeat(80));

        for (BenchmarkResult r : results) {
            System.out.printf("%-25s %15d %10d %15d %10s%n",
                    r.getLabel(),
                    r.getThroughput(),
                    r.getTimeTaken(),
                    r.getUnexpectedNulls(),
                    r.isCorrupted() ? "✗ CORRUPT" : "✓ CLEAN");
        }
        System.out.println("─".repeat(80));
    }
}
