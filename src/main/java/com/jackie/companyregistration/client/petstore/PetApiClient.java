package com.jackie.companyregistration.client.petstore;

import com.jackie.companyregistration.client.petstore.api.PetApi;
import com.jackie.companyregistration.client.petstore.model.Pet;
import com.jackie.companyregistration.config.PetstoreApiConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Application wrapper around the OpenAPI-generated {@link PetApi}, wired by
 * {@link PetstoreApiConfig}.
 */
@Service
public class PetApiClient {

    private final PetApi petApi;

    public PetApiClient(PetApi petApi) {
        this.petApi = petApi;
    }

    /**
     * Adds a pet via {@code POST /pet} on the configured Petstore base URL.
     *
     * @param pet pet to create
     * @return created pet returned by the remote API
     * @throws WebClientResponseException when the remote API responds with an error status
     */
    public Pet addPet(Pet pet) {
        return petApi.addPet(pet).block();
    }

}
