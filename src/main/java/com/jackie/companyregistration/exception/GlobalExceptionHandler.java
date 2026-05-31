package com.jackie.companyregistration.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain and validation exceptions to HTTP status codes with a {@code message} field.
 * <p>
 * Applied globally to {@code @RestController} methods; controllers do not catch these types.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** {@code GET /api/companies/requests/{id}} — unknown id or wrong client. */
    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRequestNotFound(RequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    /** {@code PUT /api/companies/{registrationNumber}} — no such company. */
    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFound(CompanyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    /** {@code POST /api/companies} — {@code clientRequestId} reused with different body. */
    @ExceptionHandler(InvalidRegistrationRequestException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRegistrationRequest(
            InvalidRegistrationRequestException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    /** Registration worker — registration number exists with a different name. */
    @ExceptionHandler(DuplicateCompanyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateCompany(DuplicateCompanyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    /** Rename or reactivate — name already used by another ACTIVE company. */
    @ExceptionHandler(DuplicateCompanyNameException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateCompanyName(DuplicateCompanyNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    /** {@code PUT /api/companies/{registrationNumber}} — company is INACTIVE. */
    @ExceptionHandler(CompanyInactiveException.class)
    public ResponseEntity<Map<String, String>> handleCompanyInactive(CompanyInactiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    /** Fallback for illegal arguments in service code. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    /** {@code @Valid} request body — first field error message returned. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

}
