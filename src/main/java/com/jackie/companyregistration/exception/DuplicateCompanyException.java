package com.jackie.companyregistration.exception;

/**
 * Registration number already registered with a different company name (worker path).
 * <p>
 * Mapped to {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class DuplicateCompanyException extends RuntimeException {

    /**
     * @param registrationNumber conflicting registration number
     */
    public DuplicateCompanyException(String registrationNumber) {
        super("Company with registration number '%s' already exists".formatted(registrationNumber));
    }

}
