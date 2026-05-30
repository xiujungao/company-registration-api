package com.jackie.companyregistration.exception;

public class RequestNotFoundException extends RuntimeException {

    public RequestNotFoundException(Long requestId) {
        super("Registration request '%d' not found".formatted(requestId));
    }

}
