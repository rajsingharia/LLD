package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

interface IRateLimiter {
    boolean giveAccess(String key);
    void shutDown();
    void updateConfig(Map<String, Object> config);
}

class TokenBucketStrategy implements IRateLimiter {
    private final int bucketCapacity;
    private final int refreshRate;
    private final Bucket globalBucket;
    private final Map<String, Bucket> buckets;
    private final int refillInMillis;
    private final ScheduledExecutorService service;

    TokenBucketStrategy(int bucketCapacity,
                        int refreshRate,
                        int refillInMillis) {
        this.bucketCapacity = bucketCapacity;
        this.refreshRate = refreshRate;
        this.globalBucket = new Bucket();
        this.buckets = new ConcurrentHashMap<>();
        this.refillInMillis = refillInMillis;
        service = Executors.newScheduledThreadPool(10);

        this.startRefillTask();
    }

    private void startRefillTask() {
        service.scheduleAtFixedRate(() -> {
            this.globalBucket.refill();
            buckets.forEach((key, bucket) -> {
                bucket.refill();
            });
        }, 0, this.refillInMillis, TimeUnit.MICROSECONDS);
    }


    private class Bucket {
        private int token;
        private final ReentrantLock lock;

        Bucket() {
            this.token = bucketCapacity;
            lock = new ReentrantLock();
        }

        public boolean tryConsuming() {
            lock.lock();
            try {
                if(token > 0) {
                    token --;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        public void refill() {
            lock.lock();
            try {
                token = Math.min(bucketCapacity, token + refreshRate);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean giveAccess(String key) {
        if(key == null || key.isEmpty()) return false;
        Bucket currentUserBucket = this.buckets.computeIfAbsent(key, (k) -> new Bucket());

        boolean gotToken = currentUserBucket.tryConsuming();
        // layer to block user or any other strategy
        return gotToken;
    }

    @Override
    public void shutDown() {
        this.service.shutdown();
    }

    @Override
    public void updateConfig(Map<String, Object> config) {
        // write logic to change config;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        TokenBucketStrategy tokenBucketStrategy = new TokenBucketStrategy(
                2,
                2,
                3000
        );

        System.out.println(tokenBucketStrategy.giveAccess("raj"));
        System.out.println(tokenBucketStrategy.giveAccess("raj"));
        System.out.println(tokenBucketStrategy.giveAccess("raj"));
        Thread.sleep(3001);
        System.out.println(tokenBucketStrategy.giveAccess("raj"));
        System.out.println(tokenBucketStrategy.giveAccess("raj"));
        System.out.println(tokenBucketStrategy.giveAccess("raj"));

        tokenBucketStrategy.shutDown();
    }
}