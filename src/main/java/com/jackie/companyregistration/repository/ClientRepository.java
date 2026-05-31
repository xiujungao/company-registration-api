package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link Client} rows in the {@code clients} table.
 * <p>
 * Used at startup by {@link com.jackie.companyregistration.security.ClientCache} to build
 * the in-memory API key index. The primary key is {@code client_id}. Standard
 * {@link JpaRepository} CRUD applies for admin or seed scripts.
 */
public interface ClientRepository extends JpaRepository<Client, String> {

    /**
     * Finds the client whose {@code api_key} column equals the given value.
     *
     * @param apiKey API key to look up (unique in {@code clients})
     * @return matching client, or empty if none
     */
    Optional<Client> findByApiKey(String apiKey);

}
