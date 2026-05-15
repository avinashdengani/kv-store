package com.avinash.kvstore.model;

public class ValueWrapper {
    private final String value;
    private final long expiryMs; // System.currentTimeMillis() + TTL

    public ValueWrapper(String value, long ttlMillis) {
        // ttlMillis = -1 means no expiry
        this.value = value;
        this.expiryMs = (ttlMillis < 0) ? -1 : System.currentTimeMillis() + ttlMillis;
    }

    public boolean isExpired() {
        return expiryMs != -1 && System.currentTimeMillis() > expiryMs;
    }

    public String getValue() {
        return value;
    }
}
