package com.jackie.companyregistration.exception;

public class DuplicateCompanyException extends RuntimeException {

    public DuplicateCompanyException(String registrationNumber) {
        super("Company with registration number '%s' already exists".formatted(registrationNumber));
    }

}
