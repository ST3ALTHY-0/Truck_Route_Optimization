package com.truckoptimization.task.api.geocodeMapsApi;

import java.util.concurrent.locks.LockSupport;

import org.springframework.stereotype.Service;

@Service
public class GeocodingRateLimiter {

    private static final long MIN_INTERVAL_NANOS = 1_100_000_000L;
    private long nextAllowedNanos = 0L;

    public synchronized void acquire() {
        long now = System.nanoTime();
        long waitNanos = nextAllowedNanos - now;

        while (waitNanos > 0L) {
            LockSupport.parkNanos(waitNanos);
            now = System.nanoTime();
            waitNanos = nextAllowedNanos - now;
        }

        nextAllowedNanos = Math.max(now, nextAllowedNanos) + MIN_INTERVAL_NANOS;
    }
}
