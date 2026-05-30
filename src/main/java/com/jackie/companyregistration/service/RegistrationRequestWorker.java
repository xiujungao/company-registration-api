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
    private final RegistrationRequestStatusService registrationRequestStatusService;
    private final RegistrationLookupOrchestrator registrationLookupOrchestrator;
    private final CompanyService companyService;

    public RegistrationRequestWorker(
            RegistrationRequestRepository registrationRequestRepository,
            RegistrationRequestStatusService registrationRequestStatusService,
            RegistrationLookupOrchestrator registrationLookupOrchestrator,
            CompanyService companyService
    ) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.registrationRequestStatusService = registrationRequestStatusService;
        this.registrationLookupOrchestrator = registrationLookupOrchestrator;
        this.companyService = companyService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long requestId) {
        var request = registrationRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return;
        }

        registrationRequestStatusService.transition(request, RequestStatus.PROCESSING, null);

        try {
            applyLookupOutcome(
                    request,
                    registrationLookupOrchestrator.resolve(
                            request.getRegistrationNumber(),
                            request.getCompanyName()
                    )
            );
        } catch (RuntimeException ex) {
            registrationRequestStatusService.transition(
                    request,
                    RequestStatus.FAILED,
                    ex.getMessage() != null ? ex.getMessage() : "Registration failed"
            );
        }
    }

    private void applyLookupOutcome(RegistrationRequest request, LookupOutcome outcome) {
        switch (outcome) {
            case LookupOutcome.LinkExisting link -> {
                request.setCompanyId(link.company().getId());
                registrationRequestStatusService.transition(request, RequestStatus.COMPLETED, null);
            }
            case LookupOutcome.Rejected rejected ->
                    registrationRequestStatusService.transition(request, RequestStatus.FAILED, rejected.message());
            case LookupOutcome.RegisterNew ignored -> registerNewCompany(request);
        }
    }

    private void registerNewCompany(RegistrationRequest request) {
        var result = companyService.register(
                new RegisterCompanyRequest(
                        request.getClientRequestId(),
                        request.getRegistrationNumber(),
                        request.getCompanyName()
                )
        );
        request.setCompanyId(result.company().id());
        registrationRequestStatusService.transition(request, RequestStatus.COMPLETED, null);
    }

}
