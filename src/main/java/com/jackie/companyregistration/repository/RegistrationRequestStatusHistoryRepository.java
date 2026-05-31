package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequestStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link RegistrationRequestStatusHistory} rows in
 * {@code registration_request_status_history}.
 * <p>
 * Append-only audit of status transitions on {@link com.jackie.companyregistration.model.RegistrationRequest}.
 * Written by {@link com.jackie.companyregistration.service.RegistrationRequestStatusService}; not exposed on
 * the public status API.
 */
public interface RegistrationRequestStatusHistoryRepository
        extends JpaRepository<RegistrationRequestStatusHistory, Long> {

    /**
     * @param registrationRequestId parent request id
     * @return history rows oldest-first
     */
    List<RegistrationRequestStatusHistory> findByRegistrationRequestIdOrderByChangedAtAsc(Long registrationRequestId);

}
