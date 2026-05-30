package com.jackie.companyregistration.config;

import com.jackie.companyregistration.repository.ApiClientRepository;
import com.jackie.companyregistration.security.ApiKeyAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ApiKeyAuthConfig {

    @Bean
    FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter(ApiClientRepository apiClientRepository) {
        var registration = new FilterRegistrationBean<>(new ApiKeyAuthFilter(apiClientRepository));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
