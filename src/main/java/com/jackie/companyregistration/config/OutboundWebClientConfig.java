package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Shared outbound {@link WebClient} with HTTPS trust material from {@code spring.ssl.bundle}.
 * <p>
 * <strong>HTTP stack:</strong> Spring reactive {@link WebClient} via {@link ClientHttpConnectorBuilder}.
 * <p>
 * <strong>Used for:</strong> building additional outbound HTTPS clients. Petstore uses
 * {@link PetstoreWebClientConfig#petstoreWebClient}; OAuth token calls use
 * {@link OutboundRestClientConfig#outboundRestClient}.
 * <p>
 * <strong>TLS / timeouts:</strong> {@code spring.http.clients.*} ({@link HttpClientSettings} from Boot auto-config).
 */
@Configuration
public class OutboundWebClientConfig {

    @Bean
    WebClient outboundWebClient(HttpClientSettings httpClientSettings) {
        var connector = ClientHttpConnectorBuilder.detect()
                .build(httpClientSettings);
        return ApiClient.buildWebClientBuilder()
                .clientConnector(connector)
                .build();
    }

}
