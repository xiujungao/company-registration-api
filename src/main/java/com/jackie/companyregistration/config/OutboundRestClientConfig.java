package com.jackie.companyregistration.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Shared outbound {@link RestClient} with HTTPS trust material from {@code spring.ssl.bundle}.
 * <p>
 * <strong>HTTP stack:</strong> Spring {@link RestClient} on JDK {@link java.net.http.HttpClient}.
 * <p>
 * <strong>Used for:</strong> OAuth2 token-endpoint calls only — {@code provider.*.token-uri}
 * (see {@link PetstoreOAuth2Config}).
 * <p>
 * <strong>TLS / timeouts:</strong> {@code spring.http.clients.*} ({@link HttpClientSettings} from Boot auto-config).
 */
@Configuration
public class OutboundRestClientConfig {

    @Bean
    RestClient outboundRestClient(HttpClientSettings httpClientSettings) {
        var requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build(httpClientSettings);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

}
