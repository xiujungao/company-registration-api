package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.admin.*} settings for administrative HTTP routes.
 * <p>
 * Bound from {@code application.yaml}, {@code env.yaml}, or environment variables
 * (for example {@code APP_ADMIN_API_KEY}).
 *
 * @param apiKey shared secret expected in the {@code X-Admin-Key} header; if blank, admin routes
 *               respond with {@code 503 Service Unavailable}
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String apiKey) {

}
