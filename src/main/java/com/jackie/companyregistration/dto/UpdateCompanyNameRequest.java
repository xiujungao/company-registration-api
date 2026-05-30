package com.jackie.companyregistration.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyNameRequest(
        @NotBlank(message = "Company name is required")
        String name
) {
}
