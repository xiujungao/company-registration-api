package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;

public record CompanyResponse(
        Long id,
        String registrationNumber,
        String name,
        CompanyStatus status
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getRegistrationNumber(),
                company.getName(),
                company.getStatus()
        );
    }

}
