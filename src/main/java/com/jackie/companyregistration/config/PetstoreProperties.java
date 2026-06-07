package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.petstore.*} settings for the generated Swagger Petstore WebClient client.
 *
 * @param baseUrl            remote Petstore API root (scheme, host, and API prefix)
 * @param apiKey             optional {@code api_key} header value for Petstore endpoints that require it
 * @param trustStorePath     optional trust store ({@code classpath:...} or {@code file:...}) for outbound TLS
 * @param trustStorePassword optional trust store password; empty string if the store has no password
 */
@ConfigurationProperties(prefix = "app.petstore")
public record PetstoreProperties(
        String baseUrl,
        String apiKey,
        String trustStorePath,
        String trustStorePassword
) {

    public PetstoreProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://petstore3.swagger.io/api/v3";
        }
    }

}
