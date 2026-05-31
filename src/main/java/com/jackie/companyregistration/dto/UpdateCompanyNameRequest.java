package com.jackie.companyregistration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code PATCH /api/companies/{registrationNumber}/name}.
 *
 * @param name new display name for the company
 */
public record UpdateCompanyNameRequest(
        @NotBlank(message = "Company name is required")
        String name
) {
}
