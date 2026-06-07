package com.jackie.companyregistration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.petstore.*} settings for the generated Swagger Petstore WebClient client.
 *
 * @param baseUrl remote Petstore API root (scheme, host, and API prefix)
 * @param apiKey  optional {@code api_key} header value for Petstore endpoints that require it
 */
@ConfigurationProperties(prefix = "app.petstore")
public record PetstoreProperties(String baseUrl, String apiKey) {

    public PetstoreProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://petstore3.swagger.io/api/v3";
        }
    }

}
