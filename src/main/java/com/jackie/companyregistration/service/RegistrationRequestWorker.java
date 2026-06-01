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

/**
 * Background processor for a single registration request: duplicate lookup, company creation, and status transitions.
 * <p>
 * Invoked from {@link RegistrationRequestService} via {@code registrationTaskExecutor}. Each
 * {@link #process(Long)} runs in its own transaction ({@code REQUIRES_NEW}).
 */
@Service
public class RegistrationRequestWorker {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final RegistrationRequestStatusService registrationRequestStatusService;
    private final RegistrationLookupOrchestrator registrationLookupOrchestrator;
    private final CompanyService companyService;

    /**
     * @param registrationRequestRepository   loads the request row by id
     * @param registrationRequestStatusService status transitions and history
     * @param registrationLookupOrchestrator parallel DB + vector lookup
     * @param companyService                  creates or matches {@link com.jackie.companyregistration.model.Company} rows
     */
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

    /**
     * Runs lookup and registration for {@code requestId} when the row is still {@link RequestStatus#PENDING}.
     * <p>
     * No-op if the row is missing or already past {@code PENDING} (idempotent for duplicate enqueue).
     * On unexpected errors, sets {@link RequestStatus#FAILED} with the exception message.
     *
     * @param requestId registration request primary key
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long requestId) {
        // Load the request row by id
        var request = registrationRequestRepository.findById(requestId).orElse(null);
        // No-op if the row is missing or already past PENDING (idempotent for duplicate enqueue)
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return;
        }

        // Transition to PROCESSING
        registrationRequestStatusService.transition(request, RequestStatus.PROCESSING, null);

        try {
            // Run the lookup and registration
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

    /** Maps orchestrator outcome to status transition and optional {@code company_id}. */
    private void applyLookupOutcome(RegistrationRequest request, LookupOutcome outcome) {
        switch (outcome) {
            case LookupOutcome.LinkExisting link -> {
                request.setCompanyId(link.company().getId());
                registrationRequestStatusService.transition(request, RequestStatus.COMPLETED, null);
            }
            case LookupOutcome.Rejected rejected ->
                    registrationRequestStatusService.transition(request, RequestStatus.FAILED, rejected.message());
            case LookupOutcome.RegisterNew _ -> registerNewCompany(request);
        }
    }

    /** Persists a new company via {@link CompanyService#register} and completes the request. */
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
