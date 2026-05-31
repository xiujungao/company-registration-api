package com.jackie.companyregistration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code PUT /api/companies/{registrationNumber}} (name in JSON only).
 *
 * @param name new display name for the company
 */
public record UpdateCompanyNameRequest(
        @NotBlank(message = "Company name is required")
        String name
) {
}
