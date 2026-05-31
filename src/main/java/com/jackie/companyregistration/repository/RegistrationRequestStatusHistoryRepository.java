package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequestStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Audit trail of status transitions for {@link com.jackie.companyregistration.model.RegistrationRequest}.
 * <p>
 * Written by {@link com.jackie.companyregistration.service.RegistrationRequestStatusService}; not exposed on the public status API.
 */
public interface RegistrationRequestStatusHistoryRepository
        extends JpaRepository<RegistrationRequestStatusHistory, Long> {

    /**
     * @param registrationRequestId parent request id
     * @return history rows oldest-first
     */
    List<RegistrationRequestStatusHistory> findByRegistrationRequestIdOrderByChangedAtAsc(Long registrationRequestId);

}
