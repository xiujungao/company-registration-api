package com.jackie.companyregistration.exception;

/**
 * No {@code companies} row for the given registration number.
 * <p>
 * Mapped to {@code 404 Not Found} by {@link GlobalExceptionHandler}.
 */
public class CompanyNotFoundException extends RuntimeException {

    /**
     * @param registrationNumber business key that was not found
     */
    public CompanyNotFoundException(String registrationNumber) {
        super("Company with registration number '%s' not found".formatted(registrationNumber));
    }

}
