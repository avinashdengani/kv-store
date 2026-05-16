# Concurrent KV Engine Exploring JVM Synchronization Strategies

A concurrent in-memory key-value store built in Java to deeply explore:

- JVM concurrency primitives
- Thread safety and synchronization
- Read-write locking strategies
- Expiration semantics (TTL)
- Concurrent collection behavior
- Background eviction systems
- Shared mutable state correctness
- Lock contention and scalability
- Performance benchmarking under multithreaded workloads

This project is inspired by the engineering concepts used in systems like:

- Redis
- ConcurrentHashMap internals
- In-memory caching engines
- Distributed metadata stores
- Backend infrastructure systems

---

# Why This Project Exists

Modern backend systems heavily rely on:

- concurrent request processing
- shared in-memory state
- caching layers
- background cleanup workers
- low-latency data access
- thread-safe synchronization

While working on backend systems, I wanted to deeply understand:

- Why `HashMap` fails under concurrency
- How synchronization impacts throughput
- Why `ConcurrentHashMap` scales better
- How `ReadWriteLock` improves read-heavy workloads
- How TTL expiration systems work internally
- How race conditions corrupt shared state
- How JVM memory visibility guarantees affect correctness
- How lock contention impacts scalability

Instead of learning these concepts only theoretically, I implemented them practically by building a concurrent in-memory KV engine from scratch.

---

# Project Goals

This project is intentionally focused on systems-oriented backend engineering concepts rather than framework-heavy application development.

The primary goals were:

- understand concurrency correctness
- explore synchronization tradeoffs
- benchmark different locking strategies
- experiment with concurrent collections
- simulate expiration management systems
- study scalability bottlenecks
- validate thread-safe behavior under load

---

# High-Level Architecture

```text
                  +-------------------+
                  |   Client Threads  |
                  +---------+---------+
                            |
                            v
                 +--------------------+
                 | Concurrent KVStore |
                 +--------------------+
                   |    |        |
                   |    |        |
              PUT/GET DELETE   TTL
                   |
                   v
         +----------------------+
         | Concurrent Storage   |
         | Layer                |
         +----------------------+
                   |
                   v
         +----------------------+
         | Expiry Manager       |
         | Background Sweeper   |
         +----------------------+
```

---

# Core Features

## Thread-Safe KV Operations

Supports concurrent:

- PUT
- GET
- DELETE

operations using multiple synchronization strategies.

---

## TTL-Based Expiration

Supports key expiration semantics similar to modern cache systems.

Example:

```java
put("user:101", value, ttl=5000);
```

The key is automatically evicted after expiration.

---

## Background Sweeper Thread

A dedicated background cleanup thread continuously scans and removes expired entries.

This simulates maintenance/eviction mechanisms commonly used in:

- Redis
- Memcached
- distributed cache systems
- in-memory metadata stores

---

## Singleton Expiry Manager

The `ExpiryManager` is implemented as a Singleton to ensure:

- centralized expiration coordination
- no duplicate cleanup workers
- reduced synchronization overhead
- predictable eviction behavior

---

## Multiple Concurrency Strategies

The project evolved through several synchronization strategies to explore correctness vs scalability tradeoffs.

Implemented approaches include:

- Plain `HashMap`
- `synchronized`
- `ReadWriteLock`
- `ConcurrentHashMap`

---

## Concurrency Benchmarking

Includes benchmarking utilities to compare:

- correctness
- contention
- scalability
- throughput

across multiple concurrent data structure implementations.

---

# Concurrency Strategy Evolution

One of the main goals of this project was understanding how different synchronization mechanisms behave under concurrent workloads.

---

## 1. Plain HashMap

Initial implementation used:

```java
HashMap<K, V>
```

This demonstrated how unsafe concurrent mutations lead to:

- race conditions
- lost updates
- inconsistent state
- bucket corruption
- unpredictable results

Observed benchmark behavior:

```text
Expected Size: 100
Actual Size: 53
```

This highlights how shared mutable state becomes corrupted without synchronization.

---

## 2. synchronized Blocks

Added coarse-grained synchronization using:

```java
synchronized(lock)
```

Benefits:

- thread safety
- correctness
- visibility guarantees

Tradeoffs:

- high lock contention
- poor scalability
- readers blocking readers
- reduced throughput

---

## 3. ReadWriteLock

Implemented `ReadWriteLock` to optimize read-heavy workloads.

Unlike synchronized locking, `ReadWriteLock` separates:

- read access
- write access

This allows:

- multiple concurrent readers
- exclusive writers only when required

Example:

```java
readLock.lock();
try {
    return map.get(key);
} finally {
    readLock.unlock();
}
```

Benefits:

- improved read scalability
- reduced contention for reads
- better throughput in read-heavy systems

Tradeoffs:

- higher coordination complexity
- possible writer starvation
- lock management overhead

This pattern is commonly useful in:

- cache systems
- metadata stores
- configuration services
- lookup-heavy backend systems

---

## 4. ConcurrentHashMap

Finally evolved toward:

```java
ConcurrentHashMap<K, V>
```

to leverage JVM-optimized concurrency mechanisms.

Benefits:

- thread-safe access
- fine-grained synchronization
- lock striping/CAS optimizations
- significantly better scalability

Compared to manual synchronization approaches, `ConcurrentHashMap` dramatically improves concurrent throughput.

---

# Why HashMap Fails Under Concurrency

`HashMap` is not thread-safe.

Concurrent writes may cause:

- race conditions
- lost updates
- inconsistent size tracking
- internal bucket corruption
- visibility issues

This project intentionally demonstrates how unsafe concurrent access corrupts shared mutable state.

---

# Why ConcurrentHashMap Scales Better

`ConcurrentHashMap` is optimized for concurrent access using:

- fine-grained synchronization
- bucket-level locking
- CAS (Compare-And-Swap)
- lock-free reads (in many cases)

Compared to synchronized locking, this significantly reduces contention under multithreaded workloads.

---

# ReadWriteLock Optimization

One major learning from this project was understanding read-heavy workload optimization.

In many backend systems:

```text
reads >> writes
```

Examples:

- caches
- configuration systems
- metadata lookups
- routing tables

Using a single synchronized lock forces:

- readers to block readers
- readers to block writers

which reduces scalability.

`ReadWriteLock` improves throughput by allowing multiple concurrent readers while still preserving write consistency.

---

# Expiration Management

The project supports TTL-based expiration semantics similar to production cache systems.

---

## Active Expiration

A background sweeper thread continuously scans for expired keys and evicts them proactively.

Benefits:

- predictable memory cleanup
- reduced stale entries
- proactive eviction

Tradeoff:

- additional background CPU usage

---

## Why Singleton ExpiryManager?

Only one expiration coordinator should exist.

This prevents:

- duplicate cleanup workers
- inconsistent eviction behavior
- thread explosion
- synchronization complexity

---

# Benchmarking Insights

The project includes concurrent benchmarking experiments validating synchronization tradeoffs.

| Strategy | Thread Safety | Read Scalability | Write Scalability |
|---|---|---|---|
| HashMap | ❌ Unsafe | High | Corrupted |
| synchronized | ✅ Safe | Poor | Poor |
| ReadWriteLock | ✅ Safe | Good | Moderate |
| ConcurrentHashMap | ✅ Safe | Excellent | Excellent |

---

# Key Concurrency Concepts Explored

This project explores several JVM and backend engineering concepts frequently discussed in SDE-2 interviews.

## Race Conditions

Multiple threads modifying shared mutable state simultaneously can produce inconsistent results.

---

## Synchronization

Used synchronization primitives to guarantee:

- mutual exclusion
- correctness
- visibility
- happens-before relationships

---

## Memory Visibility

Explored how JVM memory visibility impacts correctness across threads.

---

## Lock Contention

Benchmarked the impact of multiple threads competing for shared resources.

---

## Read vs Write Optimization

Explored how read-heavy systems benefit from separating read and write synchronization.

---

## Background Processing

Implemented asynchronous cleanup using dedicated sweeper threads.

---

# Technical Concepts Covered

This project touches multiple backend engineering domains:

- Java Concurrency
- JVM Synchronization
- ReadWriteLock
- Concurrent Collections
- Thread Safety
- Shared Mutable State
- Background Workers
- TTL Expiration
- Lock Contention
- Memory Visibility
- Performance Benchmarking
- Concurrent System Design
- Synchronization Tradeoffs

---

# Sample Use Cases

This project models simplified versions of problems solved by:

- Redis
- Memcached
- session stores
- distributed cache layers
- API response caching
- rate limiting systems
- metadata stores
- backend lookup systems

---

# Engineering Learnings

Building this project helped uncover several important backend engineering insights:

- Correctness is harder than functionality in concurrent systems
- Thread safety alone does not guarantee scalability
- Coarse-grained locking severely impacts throughput
- Read-heavy workloads benefit significantly from read-write separation
- Background cleanup systems require careful synchronization
- ConcurrentHashMap provides substantial scalability advantages
- Benchmarking is critical for validating concurrency assumptions
- Shared mutable state is one of the hardest backend engineering problems

---

# Future Enhancements

Planned future improvements include:

- LRU/LFU eviction policies
- Persistent storage (WAL)
- REST API layer
- Metrics & observability
- Distributed sharding
- Replication support
- Consistent hashing
- Lock-free experimentation
- JMH benchmarking
- Snapshot persistence
- Async replication
- Multi-node clustering

---


# Final Note

This project is not intended to compete with production KV databases.

Its primary purpose is to deeply explore:

- concurrency correctness
- JVM synchronization mechanisms
- lock contention tradeoffs
- expiration semantics
- shared mutable state management
- thread-safe backend system design
- concurrent performance characteristics

through practical implementation and benchmarking.