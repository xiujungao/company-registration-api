package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import com.jackie.companyregistration.repository.RegistrationRequestRepository;
import com.jackie.companyregistration.service.lookup.LookupOutcome;
import com.jackie.companyregistration.service.lookup.RegistrationLookupOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationRequestWorker {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final RegistrationLookupOrchestrator registrationLookupOrchestrator;
    private final CompanyService companyService;

    public RegistrationRequestWorker(
            RegistrationRequestRepository registrationRequestRepository,
            RegistrationLookupOrchestrator registrationLookupOrchestrator,
            CompanyService companyService
    ) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.registrationLookupOrchestrator = registrationLookupOrchestrator;
        this.companyService = companyService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long requestId) {
        var request = registrationRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return;
        }

        request.setStatus(RequestStatus.PROCESSING);
        registrationRequestRepository.save(request);

        try {
            applyLookupOutcome(
                    request,
                    registrationLookupOrchestrator.resolve(
                            request.getRegistrationNumber(),
                            request.getCompanyName()
                    )
            );
        } catch (RuntimeException ex) {
            request.setStatus(RequestStatus.FAILED);
            request.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : "Registration failed");
        }

        registrationRequestRepository.save(request);
    }

    private void applyLookupOutcome(RegistrationRequest request, LookupOutcome outcome) {
        switch (outcome) {
            case LookupOutcome.ExactMatch exactMatch -> applyExactMatch(request, exactMatch);
            case LookupOutcome.NoMatch ignored -> registerNewCompany(request);
        }
    }

    private void applyExactMatch(RegistrationRequest request, LookupOutcome.ExactMatch exactMatch) {
        if (exactMatch.sameName()) {
            request.setStatus(RequestStatus.COMPLETED);
            request.setCompanyId(exactMatch.company().getId());
            request.setErrorMessage(null);
            return;
        }

        request.setStatus(RequestStatus.FAILED);
        request.setErrorMessage(
                "Company with registration number '%s' already exists".formatted(request.getRegistrationNumber())
        );
    }

    private void registerNewCompany(RegistrationRequest request) {
        var result = companyService.register(
                new RegisterCompanyRequest(request.getRegistrationNumber(), request.getCompanyName())
        );
        request.setStatus(RequestStatus.COMPLETED);
        request.setCompanyId(result.company().id());
        request.setErrorMessage(null);
    }

}
