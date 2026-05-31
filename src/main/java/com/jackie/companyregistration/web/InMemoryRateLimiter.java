package com.jackie.companyregistration.web;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Fixed-window, in-memory rate limiter keyed by caller (client id, admin IP, etc.).
 * <p>
 * Suitable for a single JVM instance; not shared across replicas.
 */
@Component
public class InMemoryRateLimiter {

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /**
     * Attempts to consume one request slot for {@code key} within {@code window}.
     *
     * @param key         caller-specific bucket (for example {@code client:abc})
     * @param maxRequests allowed requests per window
     * @param window      window length
     * @return {@code true} if the request is allowed, {@code false} if the limit is exceeded
     */
    public boolean tryConsume(String key, int maxRequests, Duration window) {
        long windowMillis = window.toMillis();
        long now = System.currentTimeMillis();

        var allowed = new boolean[1];
        counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartMillis >= windowMillis) {
                allowed[0] = maxRequests >= 1;
                return new WindowCounter(now, 1);
            }
            int nextCount = current.count + 1;
            allowed[0] = nextCount <= maxRequests;
            return new WindowCounter(current.windowStartMillis, nextCount);
        });
        return allowed[0];
    }

    private record WindowCounter(long windowStartMillis, int count) {
    }

}
