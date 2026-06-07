package com.jackie.companyregistration.client.petstore;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link ServletOAuth2AuthorizedClientExchangeFilterFunction} (via {@link #oauth2Filter}) performs the
 * actual header injection on each request — this class only wires that filter onto {@link WebClient}.
 * <p>
 * Does not call {@code provider.*.token-uri}; token exchange runs on
 * {@link com.jackie.companyregistration.config.OutboundRestClientConfig#outboundRestClient}
 * ({@link org.springframework.web.client.RestClient}), not on this {@link WebClient}.
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorized-clients.html">Authorized Client Features</a>
 */
public class PetstoreOAuth2AccessTokenInterceptor {

    public static final String CLIENT_REGISTRATION_ID = "petstore";

    /**
     * On each outbound {@link WebClient} request, resolves an access token via the manager and adds
     * {@code Authorization: Bearer &lt;token&gt;} to the HTTP headers before the request is sent.
     */
    private final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter;

    public PetstoreOAuth2AccessTokenInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        oauth2Filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId(CLIENT_REGISTRATION_ID);
    }

    /** Registers {@link #oauth2Filter} on the Petstore {@link WebClient} builder. */
    public void apply(WebClient.Builder builder) {
        builder.apply(oauth2Filter.oauth2Configuration());
    }

}
