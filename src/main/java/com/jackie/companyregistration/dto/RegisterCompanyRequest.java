package com.jackie.companyregistration.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterCompanyRequest(
        @NotBlank(message = "Client request id is required")
        String clientRequestId,

        @NotBlank(message = "Registration number is required")
        String registrationNumber,

        @NotBlank(message = "Company name is required")
        String name
) {
}
