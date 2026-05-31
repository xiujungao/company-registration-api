package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.dto.RegistrationRequestResponse;
import com.jackie.companyregistration.dto.UpdateCompanyNameRequest;
import com.jackie.companyregistration.dto.CompanyResponse;
import com.jackie.companyregistration.security.ClientContext;
import com.jackie.companyregistration.service.CompanyService;
import com.jackie.companyregistration.service.RegistrationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Company registration and name updates.
 * <p>
 * Registration is asynchronous: {@link #register} returns {@code 202 Accepted} with a
 * {@code requestId} for polling via
 * {@link RegistrationRequestController}. Duplicate {@code clientRequestId} for the same client
 * returns {@code 200 OK} with the existing request. Requires API key authentication on
 * {@code POST /api/companies}; {@code PATCH} name updates are not client-scoped in this controller.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final RegistrationRequestService registrationRequestService;
    private final CompanyService companyService;

    /**
     * @param registrationRequestService submits async registration requests
     * @param companyService             synchronous company name updates
     */
    public CompanyController(
            RegistrationRequestService registrationRequestService,
            CompanyService companyService
    ) {
        this.registrationRequestService = registrationRequestService;
        this.companyService = companyService;
    }

    /**
     * Starts or returns an existing registration for {@code clientRequestId}.
     *
     * @param request     registration number, name, and client-supplied idempotency key
     * @param httpRequest current request (client id from API key)
     * @return {@code 200} when the same client payload was already submitted; {@code 202} when a new pending request was created
     */
    @PostMapping
    public ResponseEntity<RegistrationRequestResponse> register(
            @Valid @RequestBody RegisterCompanyRequest request,
            HttpServletRequest httpRequest
    ) {
        var clientId = ClientContext.getClientId(httpRequest);
        var response = registrationRequestService.submit(request, clientId);
        if (response.duplicate()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Updates the display name for an active company and records name history.
     *
     * @param registrationNumber company registration number
     * @param request            new name
     * @return updated company
     */
    @PatchMapping("/{registrationNumber}/name")
    public CompanyResponse updateName(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateCompanyNameRequest request
    ) {
        return companyService.updateName(registrationNumber, request.name());
    }

}
