package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.rate-limit.*} for per-client and admin HTTP rate limits.
 *
 * @param enabled                  when {@code false}, {@link com.jackie.companyregistration.web.RateLimitInterceptor} is a no-op
 * @param clientRequestsPerMinute  max requests per authenticated client per minute on {@code /api/*} (excluding admin)
 * @param adminRequestsPerMinute   max requests per remote address per minute on {@code /api/admin/*}
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int clientRequestsPerMinute,
        int adminRequestsPerMinute
) {

}
