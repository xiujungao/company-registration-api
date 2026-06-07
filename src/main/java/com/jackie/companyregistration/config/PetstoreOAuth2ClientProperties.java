package com.jackie.companyregistration.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Binds {@code spring.security.oauth2.client.*} from {@code application.yaml}.
 * <p>
 * {@link Registration#provider()} links each registration to a {@link Provider} by id
 * (e.g. {@code registration.petstore.provider: petstore} → {@code provider.petstore.token-uri}).
 * Token-uri values are consumed by {@link PetstoreOAuth2Config} with
 * {@link OutboundRestClientConfig#outboundRestClient}; API calls use
 * {@link PetstoreWebClientConfig#petstoreWebClient}.
 * <p>
 * Custom binding (instead of Boot {@code OAuth2ClientProperties}) allows empty {@code client-id}
 * defaults without failing startup when OAuth is disabled.
 */
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public record PetstoreOAuth2ClientProperties(
        Map<String, Registration> registration,
        Map<String, Provider> provider
) {

    public record Registration(
            String provider,
            String clientId,
            String clientSecret,
            String authorizationGrantType,
            List<String> scope
    ) {
    }

    public record Provider(String tokenUri) {
    }

    public Registration petstoreRegistration() {
        if (registration == null) {
            return null;
        }
        return registration.get(PetstoreOAuth2Config.REGISTRATION_ID);
    }

    public Provider petstoreProvider() {
        var reg = petstoreRegistration();
        if (reg == null || reg.provider() == null || provider == null) {
            return null;
        }
        return provider.get(reg.provider());
    }

    public boolean isPetstoreConfigured() {
        var reg = petstoreRegistration();
        var provider = petstoreProvider();
        return reg != null
                && StringUtils.hasText(reg.clientId())
                && provider != null
                && StringUtils.hasText(provider.tokenUri());
    }

}
