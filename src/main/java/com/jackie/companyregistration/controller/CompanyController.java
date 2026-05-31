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

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final RegistrationRequestService registrationRequestService;
    private final CompanyService companyService;

    public CompanyController(
            RegistrationRequestService registrationRequestService,
            CompanyService companyService
    ) {
        this.registrationRequestService = registrationRequestService;
        this.companyService = companyService;
    }

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

    @PatchMapping("/{registrationNumber}/name")
    public CompanyResponse updateName(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateCompanyNameRequest request
    ) {
        return companyService.updateName(registrationNumber, request.name());
    }

}
