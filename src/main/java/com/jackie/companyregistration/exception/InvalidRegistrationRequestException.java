package com.jackie.companyregistration.exception;

/**
 * Client idempotency violation: same {@code clientRequestId} reused with a different payload.
 * <p>
 * Mapped to {@code 400 Bad Request} by {@link GlobalExceptionHandler}.
 */
public class InvalidRegistrationRequestException extends RuntimeException {

    /**
     * @param message detail for the API client
     */
    public InvalidRegistrationRequestException(String message) {
        super(message);
    }

}
