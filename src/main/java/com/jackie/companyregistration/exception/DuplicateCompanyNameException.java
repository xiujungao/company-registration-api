package com.jackie.companyregistration.exception;

public class DuplicateCompanyNameException extends RuntimeException {

    public DuplicateCompanyNameException(String name) {
        super("Company name '%s' is already registered".formatted(name));
    }

}
