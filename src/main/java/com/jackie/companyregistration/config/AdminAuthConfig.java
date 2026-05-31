package com.jackie.companyregistration.config;

import com.jackie.companyregistration.security.AdminAuthFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers {@link AdminAuthFilter} for {@code /api/admin/*} routes.
 * <p>
 * Admin routes are excluded from {@link ApiKeyAuthConfig} client-key auth and instead require
 * {@link AdminAuthFilter#ADMIN_KEY_HEADER} matching {@link AdminProperties#apiKey()}.
 */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminAuthConfig {

    /**
     * Creates and registers {@link AdminAuthFilter} at the highest filter order for
     * {@code /api/admin/*} URL patterns.
     *
     * @param adminProperties admin shared secret from configuration
     * @return servlet filter registration for {@link AdminAuthFilter}
     */
    @Bean
    FilterRegistrationBean<AdminAuthFilter> adminAuthFilter(AdminProperties adminProperties) {
        var registration = new FilterRegistrationBean<>(new AdminAuthFilter(adminProperties));
        registration.addUrlPatterns("/api/admin/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
