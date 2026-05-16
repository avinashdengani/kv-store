package com.avinash.kvstore.benchmark;

import java.util.List;

public class BreakerResult {
    private final String implementation;
    private final int totalTrials;
    private final int corruptedTrials;
    private final long totalLost;
    private final int totalWriterExceptions;
    private final int totalSweeperExceptions;

    public BreakerResult(String implementation, int totalTrials,
                         int corruptedTrials, long totalLost,
                         int totalWriterExceptions, int totalSweeperExceptions) {
        this.implementation = implementation;
        this.totalTrials = totalTrials;
        this.corruptedTrials = corruptedTrials;
        this.totalLost = totalLost;
        this.totalWriterExceptions = totalWriterExceptions;
        this.totalSweeperExceptions = totalSweeperExceptions;
    }

    public String getImplementation()     { return implementation; }
    public int getTotalTrials()           { return totalTrials; }
    public int getCorruptedTrials()       { return corruptedTrials; }
    public long getTotalLost()            { return totalLost; }
    public int getTotalWriterExceptions() { return totalWriterExceptions; }
    public int getTotalSweeperExceptions(){ return totalSweeperExceptions; }
    public boolean isAlwaysClean()        { return corruptedTrials == 0; }

    public static void printSummary(List<BreakerResult> results) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println(  "║                      CONCURRENCY BREAKER SUMMARY                        ║");
        System.out.println(  "╚══════════════════════════════════════════════════════════════════════════╝");

        System.out.printf("%-25s %8s %12s %12s %12s %12s%n",
                "Implementation", "Trials", "Corrupted", "Keys Lost",
                "Writer Exc", "Sweeper Exc");
        System.out.println("─".repeat(85));

        for (BreakerResult r : results) {
            System.out.printf("%-25s %8d %12s %12d %12d %12d%n",
                    r.getImplementation(),
                    r.getTotalTrials(),
                    r.getCorruptedTrials() + "/" + r.getTotalTrials()
                            + (r.isAlwaysClean() ? " ✓" : " ✗"),
                    r.getTotalLost(),
                    r.getTotalWriterExceptions(),
                    r.getTotalSweeperExceptions()
            );
        }

        System.out.println("─".repeat(85));
        System.out.println("\n📊 Verdict:");
        for (BreakerResult r : results) {
            if (r.isAlwaysClean()) {
                System.out.println("  ✓ " + r.getImplementation()
                        + " — thread-safe. Zero data loss across all trials.");
            } else {
                System.out.println("  ✗ " + r.getImplementation()
                        + " — UNSAFE. Lost " + r.getTotalLost()
                        + " keys across " + r.getCorruptedTrials()
                        + " corrupted trials.");
            }
        }
    }
}