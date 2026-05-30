package com.jackie.companyregistration.config;

import com.jackie.companyregistration.model.ApiClient;
import com.jackie.companyregistration.repository.ApiClientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ClientSeeder implements ApplicationRunner {

    public static final String DEV_CLIENT_ID = "dev-client";
    public static final String DEV_API_KEY = "dev-api-key-change-me";

    private final ApiClientRepository apiClientRepository;

    public ClientSeeder(ApiClientRepository apiClientRepository) {
        this.apiClientRepository = apiClientRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (apiClientRepository.existsById(DEV_CLIENT_ID)) {
            return;
        }
        apiClientRepository.save(new ApiClient(DEV_CLIENT_ID, DEV_API_KEY));
    }

}
