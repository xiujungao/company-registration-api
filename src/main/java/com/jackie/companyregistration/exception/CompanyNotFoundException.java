package com.jackie.companyregistration.exception;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(String registrationNumber) {
        super("Company with registration number '%s' not found".formatted(registrationNumber));
    }

}
