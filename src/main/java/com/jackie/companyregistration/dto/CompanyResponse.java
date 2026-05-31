package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;

/**
 * JSON view of a {@link Company} for submit duplicates and name-update responses.
 */
public record CompanyResponse(
        Long id,
        String registrationNumber,
        String name,
        CompanyStatus status
) {

    /** Maps a persisted entity to the API record. */
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getRegistrationNumber(),
                company.getName(),
                company.getStatus()
        );
    }

}
