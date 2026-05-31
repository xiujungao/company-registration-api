package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.RequestStatus;

/**
 * Response body for {@code GET /api/companies/requests/{requestId}}.
 *
 * @param requestId          internal registration request id
 * @param clientRequestId    client idempotency key from submit
 * @param status             current lifecycle status
 * @param registrationNumber submitted registration number
 * @param companyName        submitted company name
 * @param errorMessage       failure reason when {@code status} is {@code FAILED}; otherwise often {@code null}
 */
public record RegistrationRequestStatusResponse(
        Long requestId,
        String clientRequestId,
        RequestStatus status,
        String registrationNumber,
        String companyName,
        String errorMessage
) {
}
