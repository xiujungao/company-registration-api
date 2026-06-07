package com.jackie.companyregistration.config;

import com.jackie.companyregistration.client.petstore.ApiClient;
import com.jackie.companyregistration.client.petstore.api.PetApi;
import com.jackie.companyregistration.client.petstore.api.StoreApi;
import com.jackie.companyregistration.client.petstore.api.UserApi;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Wires the OpenAPI-generated Petstore client ({@code mvn generate-sources}) for outbound calls.
 * <p>
 * {@link #petstoreWebClient(PetstoreProperties)} validates remote TLS certificates against
 * {@link PetstoreProperties#trustStorePath()} when configured, otherwise the JVM default CA trust store.
 */
@Configuration
@EnableConfigurationProperties(PetstoreProperties.class)
public class PetstoreClientConfig {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Bean
    WebClient petstoreWebClient(PetstoreProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .secure(ssl -> ssl.sslContext(createSslContext(properties)));
        return ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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

    private SslContext createSslContext(PetstoreProperties properties) {
        try {
            SslContextBuilder builder = SslContextBuilder.forClient();
            if (StringUtils.hasText(properties.trustStorePath())) {
                KeyStore trustStore = loadTrustStore(properties.trustStorePath(), properties.trustStorePassword());
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                builder.trustManager(trustManagerFactory);
            }
            return builder.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to configure petstore TLS", ex);
        }
    }

    private KeyStore loadTrustStore(String path, String password) {
        try {
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            Resource resource = resourceLoader.getResource(path);
            char[] passwordChars = password != null ? password.toCharArray() : new char[0];
            try (InputStream inputStream = resource.getInputStream()) {
                trustStore.load(inputStream, passwordChars);
            }
            return trustStore;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load petstore trust store: " + path, ex);
        }
    }

}
