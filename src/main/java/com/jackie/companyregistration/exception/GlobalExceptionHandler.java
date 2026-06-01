package com.jackie.companyregistration.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain and validation exceptions to HTTP status codes with a {@code message} field.
 * <p>
 * Applied globally to {@code @RestController} methods; controllers do not catch these types.
 * Status codes are declared with {@link ResponseStatus} on each handler.
 * Unmapped exceptions fall through to {@link #handleUnexpected(Exception)} ({@code 500}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** {@code GET /api/companies/requests/{id}} — unknown id or wrong client. */
    @ExceptionHandler(RequestNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleRequestNotFound(RequestNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** {@code PUT /api/companies/{registrationNumber}} — no such company. */
    @ExceptionHandler(CompanyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCompanyNotFound(CompanyNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** {@code POST /api/companies} — {@code clientRequestId} reused with different body. */
    @ExceptionHandler(InvalidRegistrationRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRegistrationRequest(InvalidRegistrationRequestException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** Registration worker — registration number exists with a different name. */
    @ExceptionHandler(DuplicateCompanyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateCompany(DuplicateCompanyException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** Rename or reactivate — name already used by another ACTIVE company. */
    @ExceptionHandler(DuplicateCompanyNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateCompanyName(DuplicateCompanyNameException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** {@code PUT /api/companies/{registrationNumber}} — company is INACTIVE. */
    @ExceptionHandler(CompanyInactiveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleCompanyInactive(CompanyInactiveException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** Fallback for illegal arguments in service code. */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    /** {@code @Valid} request body — all field errors returned under {@code errors}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid",
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        var body = new LinkedHashMap<String, Object>();
        body.put("message", "Validation failed");
        body.put("errors", errors);
        return body;
    }

    /** Catch-all for unmapped exceptions; details logged server-side only. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return Map.of("message", "Internal server error");
    }

}
