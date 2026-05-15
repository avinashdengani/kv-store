package com.avinash.kvstore.benchmark;

import com.avinash.kvstore.store.KVStore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyBreaker {
    public static void main(String[] args) throws InterruptedException {
        KVStore kvStore = new KVStore();
        int threadCount = 50, opsPerThread=10000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        System.out.println("Starting " + threadCount + " threads...");

        for(int t=0; t<threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
               try {
                   for(int i=0; i<opsPerThread; i++) {
                       String key = "key" + (i%100);

                       if(i%2 == 0) {
                           kvStore.set(key, "value_" + threadId, -1);
                       } else  {
                           kvStore.get(key);
                       }
                   }
               } catch (Exception e) {
                   System.out.println("EXCEPTION in thread " + threadId
                           + ": " + e.getClass().getSimpleName()
                           + " — " + e.getMessage());
               } finally {
                   latch.countDown();
               }
            });
        }

        latch.await();
        executor.shutdown();
        System.out.println("Done. Final store size: " + kvStore.size());
        System.out.println("Expected max size: 100");

    }
}
