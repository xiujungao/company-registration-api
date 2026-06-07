package com.jackie.companyregistration.config;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Shared outbound {@link RestClient} with HTTPS trust material from {@code spring.ssl.bundle}.
 * <p>
 * <strong>HTTP stack:</strong> Spring {@link RestClient} on {@link java.net.http.HttpClient}
 * ({@link JdkClientHttpRequestFactory}).
 * <p>
 * <strong>Used for:</strong> OAuth2 token-endpoint calls only — {@code provider.*.token-uri}
 * (see {@link PetstoreOAuth2Config}). Spring Security fetches access tokens on this client, not on
 * {@link OutboundWebClientConfig#outboundWebClient} or {@link PetstoreWebClientConfig#petstoreWebClient}.
 * <p>
 * <strong>TLS:</strong> {@code app.outbound.ssl-bundle} (same bundle as the WebClient beans).
 * Works for headless/service apps that do not expose HTTP APIs.
 */
@Configuration
@EnableConfigurationProperties(OutboundSslProperties.class)
public class OutboundRestClientConfig {

    @Bean
    RestClient outboundRestClient(SslBundles sslBundles, OutboundSslProperties properties) {
        SslBundle sslBundle = sslBundles.getBundle(properties.sslBundle());
        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslBundle.createSslContext())
                .build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

}
