package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.dto.RegistrationRequestStatusResponse;
import com.jackie.companyregistration.security.ClientContext;
import com.jackie.companyregistration.service.RegistrationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Poll registration request status for the authenticated API client.
 * <p>
 * Paths are under {@code /api/companies/requests} and require a valid API key
 * ({@link com.jackie.companyregistration.security.ApiKeyAuthFilter}). Status is scoped by
 * {@link com.jackie.companyregistration.security.ClientContext#getClientId(jakarta.servlet.http.HttpServletRequest)};
 * clients cannot read another client's {@code requestId}.
 */
@RestController
@RequestMapping("/api/companies/requests")
public class RegistrationRequestController {

    private final RegistrationRequestService registrationRequestService;

    /**
     * @param registrationRequestService loads requests and status history for the caller's client
     */
    public RegistrationRequestController(RegistrationRequestService registrationRequestService) {
        this.registrationRequestService = registrationRequestService;
    }

    /**
     * Returns current status and registration payload fields.
     *
     * @param requestId    registration request primary key from {@code POST /api/companies}
     * @param httpRequest  current request (client id set by API key filter)
     * @return status payload for {@code requestId} when owned by the caller
     */
    @GetMapping("/{requestId}")
    public RegistrationRequestStatusResponse getStatus(
            @PathVariable Long requestId,
            HttpServletRequest httpRequest
    ) {
        var clientId = ClientContext.getClientId(httpRequest);
        return registrationRequestService.getStatus(requestId, clientId);
    }

}
