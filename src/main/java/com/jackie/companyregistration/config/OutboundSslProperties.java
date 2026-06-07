package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.outbound.*} TLS settings shared by all outbound HTTPS clients.
 * <p>
 * {@link #sslBundle} names a {@code spring.ssl.bundle} entry applied to
 * {@link OutboundRestClientConfig} (OAuth {@code token-uri}), {@link OutboundWebClientConfig}, and
 * {@link PetstoreWebClientConfig}.
 *
 * @param sslBundle name of the {@code spring.ssl.bundle} used for TLS trust material
 */
@ConfigurationProperties(prefix = "app.outbound")
public record OutboundSslProperties(String sslBundle) {

    public OutboundSslProperties {
        if (sslBundle == null || sslBundle.isBlank()) {
            sslBundle = "client-mtls";
        }
    }

}
