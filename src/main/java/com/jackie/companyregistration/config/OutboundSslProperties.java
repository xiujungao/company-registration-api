package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.outbound.*} settings shared by outbound HTTPS {@link org.springframework.web.reactive.function.client.WebClient} clients.
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
