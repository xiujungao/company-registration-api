package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    Optional<RegistrationRequest> findByIdAndClientId(Long id, String clientId);

    Optional<RegistrationRequest> findByClientIdAndClientRequestId(String clientId, String clientRequestId);

}
