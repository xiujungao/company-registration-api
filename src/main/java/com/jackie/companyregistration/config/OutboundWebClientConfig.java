package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webclient.autoconfigure.WebClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Shared outbound {@link WebClient} with HTTPS trust material from {@code spring.ssl.bundle}.
 * <p>
 * <strong>HTTP stack:</strong> Spring reactive {@link WebClient} (Reactor Netty connector via
 * {@link WebClientSsl}) — not {@link java.net.http.HttpClient} and not {@link RestClient}.
 * <p>
 * <strong>Used for:</strong> building additional outbound HTTPS clients that share the same TLS
 * trust store. Petstore uses {@link PetstoreWebClientConfig#petstoreWebClient} instead; OAuth token
 * calls use {@link OutboundRestClientConfig#outboundRestClient}.
 * <p>
 * <strong>TLS:</strong> {@code app.outbound.ssl-bundle}.
 */
@Configuration
@EnableConfigurationProperties(OutboundSslProperties.class)
public class OutboundWebClientConfig {

    @Bean
    WebClient outboundWebClient(OutboundSslProperties properties, WebClientSsl webClientSsl) {
        return ApiClient.buildWebClientBuilder()
                .apply(webClientSsl.fromBundle(properties.sslBundle()))
                .build();
    }

}
