package com.jackie.companyregistration.config;

import com.jackie.companyregistration.web.RateLimitInterceptor;
import com.jackie.companyregistration.web.RequestLoggingInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers MVC interceptors for API routes.
 * <p>
 * Logging runs first so {@link RequestLoggingInterceptor#afterCompletion} still records
 * {@code 429} responses from {@link RateLimitInterceptor}. Auth remains in servlet filters
 * ({@link com.jackie.companyregistration.config.ApiKeyAuthConfig},
 * {@link com.jackie.companyregistration.config.AdminAuthConfig}).
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(
            RequestLoggingInterceptor requestLoggingInterceptor,
            RateLimitInterceptor rateLimitInterceptor
    ) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

}
