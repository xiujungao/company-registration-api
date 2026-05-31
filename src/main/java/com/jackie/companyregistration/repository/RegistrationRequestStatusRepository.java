package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.RegistrationRequestStatusEntity;
import com.jackie.companyregistration.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link RegistrationRequestStatusEntity} rows in
 * {@code registration_request_statuses}.
 * <p>
 * Lookup table seeded from {@link RequestStatus} ({@code PENDING}, {@code PROCESSING},
 * {@code COMPLETED}, {@code FAILED}). {@link com.jackie.companyregistration.service.RegistrationRequestStatusService}
 * uses {@link JpaRepository#getReferenceById(Object)} with {@link RequestStatus#name()} as the
 * foreign-key value on {@code registration_requests.status_code}.
 */
public interface RegistrationRequestStatusRepository
        extends JpaRepository<RegistrationRequestStatusEntity, String> {
}
