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
 * Inject {@code outboundWebClient} or build additional clients with the same {@link WebClientSsl} bundle.
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
