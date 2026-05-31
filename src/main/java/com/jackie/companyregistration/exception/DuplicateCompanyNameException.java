package com.jackie.companyregistration.exception;

/**
 * Another ACTIVE company already uses the requested display name.
 * <p>
 * Mapped to {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class DuplicateCompanyNameException extends RuntimeException {

    /**
     * @param name display name that is already taken among ACTIVE companies
     */
    public DuplicateCompanyNameException(String name) {
        super("Company name '%s' is already registered".formatted(name));
    }

}
