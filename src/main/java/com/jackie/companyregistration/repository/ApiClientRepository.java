package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.ApiClient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiClientRepository extends JpaRepository<ApiClient, String> {

    Optional<ApiClient> findByApiKey(String apiKey);

}
