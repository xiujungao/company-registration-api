package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    Optional<RegistrationRequest> findByIdAndClientId(Long id, String clientId);

    Optional<RegistrationRequest> findFirstByClientIdAndRegistrationNumberAndCompanyNameAndStatusInOrderByCreatedAtDesc(
            String clientId,
            String registrationNumber,
            String companyName,
            Collection<RequestStatus> statuses
    );

    Optional<RegistrationRequest> findFirstByClientIdAndRegistrationNumberAndStatusOrderByCreatedAtDesc(
            String clientId,
            String registrationNumber,
            RequestStatus status
    );

}
