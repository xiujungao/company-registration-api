package com.jackie.companyregistration.client.petstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jackie.companyregistration.client.petstore.model.Pet;
import com.jackie.companyregistration.config.OutboundWebClientConfig;
import com.jackie.companyregistration.config.PetstoreApiConfig;
import com.jackie.companyregistration.config.PetstoreWebClientConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Calls the live Swagger Petstore ({@code https://petstore3.swagger.io/api/v3}) through
 * {@link PetApiClient}. Requires network access.
 */
@Tag("live")
@ImportAutoConfiguration({SslAutoConfiguration.class, HttpClientAutoConfiguration.class, WebClientAutoConfiguration.class})
@SpringBootTest(classes = {
        OutboundWebClientConfig.class,
        PetstoreWebClientConfig.class,
        PetstoreApiConfig.class,
        PetApiClient.class
})
@TestPropertySource(properties = {
        "app.petstore.base-url=https://petstore3.swagger.io/api/v3",
        "spring.http.clients.ssl.bundle=client-mtls",
        "spring.ssl.bundle.jks.client-mtls.truststore.location=classpath:ssl/truststore.p12",
        "spring.ssl.bundle.jks.client-mtls.truststore.password=changeit",
        "spring.ssl.bundle.jks.client-mtls.truststore.type=PKCS12"
})
class PetApiAddPetTest {

    @Autowired
    private PetApiClient petApiClient;

    @Test
    void addPet_callsLivePetstoreAndReturnsPet() {
        String petName = "doggie-" + UUID.randomUUID();
        long petId = System.currentTimeMillis();

        // Live petstore3.swagger.io requires client-supplied id on POST /pet (otherwise 500).
        var requestPet = new Pet()
                .id(petId)
                .name(petName)
                .photoUrls(List.of("https://example.com/dog.jpg"))
                .status(Pet.StatusEnum.AVAILABLE);

        Pet response = petApiClient.addPet(requestPet);

        assertNotNull(response);
        assertEquals(petId, response.getId());
        assertEquals(petName, response.getName());
        assertEquals(Pet.StatusEnum.AVAILABLE, response.getStatus());
    }

}
