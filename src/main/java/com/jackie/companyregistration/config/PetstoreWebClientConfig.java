package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import com.jackie.companyregistration.client.petstore.PetstoreOAuth2AccessTokenInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Petstore API outbound {@link WebClient}: SSL bundle plus optional OAuth2 bearer interceptor.
 * <p>
 * <strong>Used for:</strong> remote Petstore HTTP calls ({@code app.petstore.base-url}).
 * <p>
 * <strong>TLS / timeouts:</strong> {@code spring.http.clients.*} ({@link HttpClientSettings} from Boot auto-config).
 */
@Configuration
@EnableConfigurationProperties(PetstoreOAuth2ClientProperties.class)
public class PetstoreWebClientConfig {

    @Bean
    WebClient petstoreWebClient(
            HttpClientSettings httpClientSettings,
            ObjectProvider<PetstoreOAuth2AccessTokenInterceptor> oauth2Interceptor) {
        var connector = ClientHttpConnectorBuilder.detect()
                .build(httpClientSettings);
        WebClient.Builder builder = ApiClient.buildWebClientBuilder()
                .clientConnector(connector);
        oauth2Interceptor.ifAvailable(interceptor -> interceptor.apply(builder));
        return builder.build();
    }

}
