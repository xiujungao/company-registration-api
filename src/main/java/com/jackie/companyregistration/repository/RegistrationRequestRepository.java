package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link RegistrationRequest} rows in {@code registration_requests}.
 * <p>
 * Used by {@link com.jackie.companyregistration.service.RegistrationRequestService} for submit
 * idempotency ({@code client_id + client_request_id}) and client-scoped status polling.
 */
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    /**
     * Loads a request only when it belongs to the given API client.
     *
     * @param id       registration request primary key
     * @param clientId authenticated client id
     * @return matching row, or empty if missing or owned by another client
     */
    Optional<RegistrationRequest> findByIdAndClientId(Long id, String clientId);

    /**
     * Finds a prior submit for the same client idempotency key.
     *
     * @param clientId        authenticated client id
     * @param clientRequestId client-supplied idempotency key
     * @return existing request, or empty if this key was never used
     */
    Optional<RegistrationRequest> findByClientIdAndClientRequestId(String clientId, String clientRequestId);

}
