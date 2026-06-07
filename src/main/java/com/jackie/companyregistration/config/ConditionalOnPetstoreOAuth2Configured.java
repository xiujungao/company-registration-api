package com.jackie.companyregistration.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Enables {@link PetstoreOAuth2Config} when both OAuth registration credentials and a token endpoint
 * are present in {@code application.yaml}. Avoids loading OAuth beans (and
 * {@link OutboundRestClientConfig#outboundRestClient} token traffic) when env vars are unset.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPetstoreOAuth2ConfiguredCondition.class)
@interface ConditionalOnPetstoreOAuth2Configured {
}

final class OnPetstoreOAuth2ConfiguredCondition implements ConfigurationCondition {

    static final String CLIENT_ID_PROPERTY =
            "spring.security.oauth2.client.registration.petstore.client-id";
    static final String TOKEN_URI_PROPERTY =
            "spring.security.oauth2.client.provider.petstore.token-uri";

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var environment = context.getEnvironment();
        return hasConfiguredValue(environment.getProperty(CLIENT_ID_PROPERTY))
                && hasConfiguredValue(environment.getProperty(TOKEN_URI_PROPERTY));
    }

    private static boolean hasConfiguredValue(String value) {
        return StringUtils.hasText(value) && !value.startsWith("${");
    }

}
