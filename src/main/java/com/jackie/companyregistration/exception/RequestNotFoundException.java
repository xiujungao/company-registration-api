package com.jackie.companyregistration.exception;

/**
 * Registration request id missing or not owned by the authenticated client.
 * <p>
 * Mapped to {@code 404 Not Found} by {@link GlobalExceptionHandler}.
 */
public class RequestNotFoundException extends RuntimeException {

    /**
     * @param requestId internal registration request id from the API
     */
    public RequestNotFoundException(Long requestId) {
        super("Registration request '%d' not found".formatted(requestId));
    }

}
