package com.jackie.companyregistration.exception;

/**
 * Thrown when an operation requires an {@link com.jackie.companyregistration.model.CompanyStatus#ACTIVE}
 * company but the row is {@link com.jackie.companyregistration.model.CompanyStatus#INACTIVE}.
 * <p>
 * Mapped to {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class CompanyInactiveException extends RuntimeException {

    /**
     * @param registrationNumber registration number of the inactive company
     */
    public CompanyInactiveException(String registrationNumber) {
        super(("Company with registration number '%s' is inactive; "
                + "submit a new registration to reactivate before renaming")
                .formatted(registrationNumber));
    }

}
