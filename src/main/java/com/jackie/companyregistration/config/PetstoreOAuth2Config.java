package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.PetstoreOAuth2AccessTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestClient;

/**
 * OAuth2 {@code client_credentials} for Petstore when
 * {@code spring.security.oauth2.client.registration.petstore.client-id} and
 * {@code provider.petstore.token-uri} are both configured ({@link ConditionalOnPetstoreOAuth2Configured}).
 * <p>
 * <strong>Token HTTP client:</strong> {@link OutboundRestClientConfig#outboundRestClient}
 * ({@link RestClient} + JDK {@link java.net.http.HttpClient}) is passed to
 * {@link RestClientClientCredentialsTokenResponseClient} so TLS to {@code token-uri} uses
 * {@code app.outbound.ssl-bundle}. Uses {@link AuthorizedClientServiceOAuth2AuthorizedClientManager}
 * (no servlet/API required — suitable for background/service apps).
 * <p>
 * <strong>API HTTP client:</strong> not configured here; bearer tokens are attached on
 * {@link PetstoreWebClientConfig#petstoreWebClient} via {@link PetstoreOAuth2AccessTokenInterceptor}.
 */
@Configuration
@ConditionalOnPetstoreOAuth2Configured
public class PetstoreOAuth2Config {

    static final String REGISTRATION_ID = "petstore";

    @Bean
    ClientRegistrationRepository petstoreClientRegistrationRepository(PetstoreOAuth2ClientProperties properties) {
        var reg = properties.petstoreRegistration();
        var provider = properties.petstoreProvider();
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(reg.clientId())
                .clientSecret(reg.clientSecret())
                .authorizationGrantType(new AuthorizationGrantType(reg.authorizationGrantType()))
                .tokenUri(provider.tokenUri())
                .scope(reg.scope())
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    OAuth2AuthorizedClientService petstoreOAuth2AuthorizedClientService(
            ClientRegistrationRepository petstoreClientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(petstoreClientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientManager petstoreOAuth2AuthorizedClientManager(
            ClientRegistrationRepository petstoreClientRegistrationRepository,
            OAuth2AuthorizedClientService petstoreOAuth2AuthorizedClientService,
            RestClient outboundRestClient) {
        RestClientClientCredentialsTokenResponseClient tokenResponseClient =
                new RestClientClientCredentialsTokenResponseClient();
        tokenResponseClient.setRestClient(outboundRestClient);

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(clientCredentials -> clientCredentials
                        .accessTokenResponseClient(tokenResponseClient))
                .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        petstoreClientRegistrationRepository, petstoreOAuth2AuthorizedClientService);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    @Bean
    PetstoreOAuth2AccessTokenInterceptor petstoreOAuth2AccessTokenInterceptor(
            OAuth2AuthorizedClientManager petstoreOAuth2AuthorizedClientManager) {
        return new PetstoreOAuth2AccessTokenInterceptor(petstoreOAuth2AuthorizedClientManager);
    }

}
