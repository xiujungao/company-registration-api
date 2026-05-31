package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import java.time.Instant;

/**
 * Response body for {@code POST /api/companies}.
 *
 * @param requestId          internal id for polling status
 * @param clientRequestId    echoed idempotency key
 * @param status             status at submit time (usually {@code PENDING})
 * @param registrationNumber echoed registration number
 * @param createdAt          row creation time
 * @param duplicate          {@code true} when an existing row matched {@code clientRequestId}
 * @param message            human-readable note on duplicate submits
 * @param company            resolved company on duplicate completed requests; otherwise often {@code null}
 */
public record RegistrationRequestResponse(
        Long requestId,
        String clientRequestId,
        RequestStatus status,
        String registrationNumber,
        Instant createdAt,
        boolean duplicate,
        String message,
        CompanyResponse company
) {

    /** Builds the response for a newly created pending request ({@code 202 Accepted}). */
    public static RegistrationRequestResponse fromNew(RegistrationRequest request) {
        return new RegistrationRequestResponse(
                request.getId(),
                request.getClientRequestId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCreatedAt(),
                false,
                null,
                null
        );
    }

    /**
     * Builds the response when {@code clientRequestId} was already submitted ({@code 200 OK}).
     */
    public static RegistrationRequestResponse duplicate(
            RegistrationRequest request,
            String message,
            CompanyResponse company
    ) {
        return new RegistrationRequestResponse(
                request.getId(),
                request.getClientRequestId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCreatedAt(),
                true,
                message,
                company
        );
    }

}
