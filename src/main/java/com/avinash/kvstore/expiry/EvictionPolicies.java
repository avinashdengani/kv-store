package com.avinash.kvstore.expiry;

public class EvictionPolicies {

    // TTL-based — evict if expired
    public static EvictionPolicy ttlBased() {
        return (key, value) -> value.isExpired();
    }

    // Prefix-based — evict all keys with a given prefix
    public static EvictionPolicy prefixBased(String prefix) {
        return (key, value) -> key.startsWith(prefix);
    }

    // Combined — evict if either condition is true
    public static EvictionPolicy combined(EvictionPolicy a, EvictionPolicy b) {
        return (key, value) -> a.shouldEvict(key, value) || b.shouldEvict(key, value);
    }

}
