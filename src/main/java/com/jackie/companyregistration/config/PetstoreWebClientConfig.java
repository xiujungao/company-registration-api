package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import com.jackie.companyregistration.client.petstore.PetstoreOAuth2AccessTokenInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webclient.autoconfigure.WebClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Petstore API outbound {@link WebClient}: SSL bundle plus optional OAuth2 bearer interceptor.
 * <p>
 * <strong>HTTP stack:</strong> Spring reactive {@link WebClient} (same as
 * {@link OutboundWebClientConfig}, via {@link WebClientSsl}).
 * <p>
 * <strong>Used for:</strong> remote Petstore HTTP calls ({@code app.petstore.base-url}) — wired into
 * the OpenAPI client by {@link PetstoreApiConfig}. When OAuth2 is enabled, adds
 * {@code Authorization: Bearer} via {@link PetstoreOAuth2AccessTokenInterceptor}; does <em>not</em>
 * call {@code provider.*.token-uri} (that is {@link OutboundRestClientConfig#outboundRestClient}).
 * <p>
 * <strong>TLS:</strong> {@code app.outbound.ssl-bundle}.
 */
@Configuration
@EnableConfigurationProperties({OutboundSslProperties.class, PetstoreOAuth2ClientProperties.class})
public class PetstoreWebClientConfig {

    @Bean
    WebClient petstoreWebClient(
            OutboundSslProperties sslProperties,
            WebClientSsl webClientSsl,
            ObjectProvider<PetstoreOAuth2AccessTokenInterceptor> oauth2Interceptor) {
        WebClient.Builder builder = ApiClient.buildWebClientBuilder()
                .apply(webClientSsl.fromBundle(sslProperties.sslBundle()));
        oauth2Interceptor.ifAvailable(interceptor -> interceptor.apply(builder));
        return builder.build();
    }

}
