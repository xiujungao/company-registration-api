package com.jackie.companyregistration.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain and validation exceptions to HTTP status codes with a {@code message} field.
 * <p>
 * Applied globally to {@code @RestController} methods; controllers do not catch these types.
 * Status codes are declared with {@link ResponseStatus} on each handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    /** {@code @Valid} request body — first field error message returned. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");
        return Map.of("message", message);
    }

}
