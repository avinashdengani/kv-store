package com.avinash.kvstore.expiry;

@FunctionalInterface
public interface SweeperExceptionHandler {
    void handle(Exception e);
}