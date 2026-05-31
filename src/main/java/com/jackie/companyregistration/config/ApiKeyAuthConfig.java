package com.jackie.companyregistration.config;

import com.jackie.companyregistration.security.ClientCache;
import com.jackie.companyregistration.security.ApiKeyAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers {@link ApiKeyAuthFilter} as a servlet filter for API routes.
 * <p>
 * The filter validates the {@code X-API-Key} header against {@link ClientCache} (loaded from
 * the {@code clients} table at startup) before controller code runs. Paths under
 * {@code /api/admin/} are excluded here and authenticated by {@link AdminAuthConfig} instead.
 */
@Configuration
public class ApiKeyAuthConfig {

    /**
     * Creates and registers {@link ApiKeyAuthFilter} for {@code /api/*} URLs at the highest
     * filter order so authentication runs before other servlet filters.
     *
     * @param clientCache in-memory API key index populated from the database at startup
     * @return servlet filter registration for {@link ApiKeyAuthFilter}
     */
    @Bean
    FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter(ClientCache clientCache) {
        var registration = new FilterRegistrationBean<>(new ApiKeyAuthFilter(clientCache));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
