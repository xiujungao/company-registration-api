package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import com.jackie.companyregistration.client.petstore.api.PetApi;
import com.jackie.companyregistration.client.petstore.api.StoreApi;
import com.jackie.companyregistration.client.petstore.api.UserApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Wires the OpenAPI-generated Petstore client ({@code mvn generate-sources}) for outbound calls.
 * <p>
 * Generated sources live under {@code com.jackie.companyregistration.client.petstore}. Inject
 * {@link PetApi}, {@link StoreApi}, or {@link UserApi} to invoke the remote API.
 */
@Configuration
@EnableConfigurationProperties(PetstoreProperties.class)
public class PetstoreClientConfig {

    @Bean
    WebClient petstoreWebClient() {
        return ApiClient.buildWebClient();
    }

    @Bean
    ApiClient petstoreApiClient(WebClient petstoreWebClient, PetstoreProperties properties) {
        var client = new ApiClient(petstoreWebClient);
        client.setBasePath(properties.baseUrl());
        if (StringUtils.hasText(properties.apiKey())) {
            client.setApiKey(properties.apiKey());
        }
        return client;
    }

    @Bean
    PetApi petApi(ApiClient petstoreApiClient) {
        return new PetApi(petstoreApiClient);
    }

    @Bean
    StoreApi storeApi(ApiClient petstoreApiClient) {
        return new StoreApi(petstoreApiClient);
    }

    @Bean
    UserApi userApi(ApiClient petstoreApiClient) {
        return new UserApi(petstoreApiClient);
    }

}
