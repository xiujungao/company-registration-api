package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.Company;

public record CompanyResponse(
        Long id,
        String registrationNumber,
        String name
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getRegistrationNumber(),
                company.getName()
        );
    }

}
