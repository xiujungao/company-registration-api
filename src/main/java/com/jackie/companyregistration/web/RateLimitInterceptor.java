package com.jackie.companyregistration.web;

import com.jackie.companyregistration.config.RateLimitProperties;
import com.jackie.companyregistration.security.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Applies per-minute rate limits on {@code /api/**} routes after authentication filters run.
 * <p>
 * Client routes are limited by {@link ClientContext#getClientId(HttpServletRequest)}; admin routes
 * are limited by remote address. Responds with {@code 429 Too Many Requests} when exceeded.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private final RateLimitProperties properties;
    private final InMemoryRateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimitProperties properties, InMemoryRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!properties.enabled()) {
            return true;
        }

        var key = resolveKey(request);
        var limit = resolveLimit(request);
        if (rateLimiter.tryConsume(key, limit, ONE_MINUTE)) {
            return true;
        }

        writeTooManyRequests(response);
        return false;
    }

    private String resolveKey(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/admin/")) {
            return "admin:" + request.getRemoteAddr();
        }

        var clientId = ClientContext.getClientId(request);
        if (clientId != null) {
            return "client:" + clientId;
        }

        return "ip:" + request.getRemoteAddr();
    }

    private int resolveLimit(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/admin/")) {
            return properties.adminRequestsPerMinute();
        }
        return properties.clientRequestsPerMinute();
    }

    private static void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write("{\"message\":\"Rate limit exceeded\"}");
    }

}
