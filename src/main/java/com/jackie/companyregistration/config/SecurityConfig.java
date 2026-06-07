package com.jackie.companyregistration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Prevents Spring Security from securing inbound API routes when {@code spring-boot-starter-oauth2-client}
 * is on the classpath. Inbound auth stays on servlet filters ({@link ApiKeyAuthConfig},
 * {@link AdminAuthConfig}). Outbound OAuth2 (token-uri + bearer on Petstore API) is configured in
 * {@link PetstoreOAuth2Config} / {@link PetstoreWebClientConfig} — not here.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
