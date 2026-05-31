package com.jackie.companyregistration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/companies}.
 *
 * @param clientRequestId    client idempotency key (unique per {@code client_id})
 * @param registrationNumber government or business registration number
 * @param name               company display name
 */
public record RegisterCompanyRequest(
        @NotBlank(message = "Client request id is required")
        String clientRequestId,

        @NotBlank(message = "Registration number is required")
        String registrationNumber,

        @NotBlank(message = "Company name is required")
        String name
) {
}
