package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.dto.RegistrationRequestStatusResponse;
import com.jackie.companyregistration.security.ClientContext;
import com.jackie.companyregistration.service.RegistrationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/requests")
public class RegistrationRequestController {

    private final RegistrationRequestService registrationRequestService;

    public RegistrationRequestController(RegistrationRequestService registrationRequestService) {
        this.registrationRequestService = registrationRequestService;
    }

    @GetMapping("/{requestId}")
    public RegistrationRequestStatusResponse getStatus(
            @PathVariable Long requestId,
            HttpServletRequest httpRequest
    ) {
        var clientId = ClientContext.getClientId(httpRequest);
        return registrationRequestService.getStatus(requestId, clientId);
    }

}
