package com.jackie.companyregistration.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryRateLimiterTest {

    private InMemoryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new InMemoryRateLimiter();
    }

    @Test
    void allowsRequestsUpToLimit() {
        var key = "client:test";
        var window = Duration.ofMinutes(1);

        assertTrue(rateLimiter.tryConsume(key, 2, window));
        assertTrue(rateLimiter.tryConsume(key, 2, window));
        assertFalse(rateLimiter.tryConsume(key, 2, window));
    }

    @Test
    void isolatesKeys() {
        var window = Duration.ofMinutes(1);

        assertTrue(rateLimiter.tryConsume("client:a", 1, window));
        assertFalse(rateLimiter.tryConsume("client:a", 1, window));
        assertTrue(rateLimiter.tryConsume("client:b", 1, window));
    }

}
