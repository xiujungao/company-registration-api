package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;
import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.dto.RegistrationRequestResponse;
import com.jackie.companyregistration.dto.RegistrationRequestStatusResponse;
import com.jackie.companyregistration.exception.InvalidRegistrationRequestException;
import com.jackie.companyregistration.exception.RequestNotFoundException;
import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import com.jackie.companyregistration.repository.CompanyRepository;
import com.jackie.companyregistration.repository.RegistrationRequestRepository;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Orchestrates async company registration: idempotent submit, status queries, and hand-off to the worker pool.
 * <p>
 * {@link #submit} persists a {@code PENDING} row (or returns a prior match on
 * {@code clientId + clientRequestId}), then runs
 * {@link RegistrationRequestWorker#process(long)} on {@code registrationTaskExecutor}. Status
 * reads are transactional and limited to the owning client.
 */
@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final CompanyRepository companyRepository;
    private final RegistrationRequestWorker registrationRequestWorker;
    private final RegistrationRequestStatusService registrationRequestStatusService;
    private final Executor registrationTaskExecutor;
    private final TransactionTemplate transactionTemplate;

    /**
     * @param registrationTaskExecutor background pool from {@link com.jackie.companyregistration.config.AsyncConfig}
     */
    public RegistrationRequestService(
            RegistrationRequestRepository registrationRequestRepository,
            CompanyRepository companyRepository,
            RegistrationRequestWorker registrationRequestWorker,
            RegistrationRequestStatusService registrationRequestStatusService,
            @Qualifier("registrationTaskExecutor") Executor registrationTaskExecutor,
            PlatformTransactionManager transactionManager
    ) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.companyRepository = companyRepository;
        this.registrationRequestWorker = registrationRequestWorker;
        this.registrationRequestStatusService = registrationRequestStatusService;
        this.registrationTaskExecutor = registrationTaskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Creates a pending registration or returns an existing one for the same client idempotency key.
     *
     * @param request  payload including {@code clientRequestId}
     * @param clientId authenticated client from {@link com.jackie.companyregistration.security.ClientContext}
     * @return new request ({@code duplicate=false}) or prior match ({@code duplicate=true})
     */
    public RegistrationRequestResponse submit(RegisterCompanyRequest request, String clientId) {
        var existingByClientRequestId = findExistingByClientRequestId(clientId, request);
        if (existingByClientRequestId != null) {
            return existingByClientRequestId;
        }

        RegistrationRequestResponse response;
        try {
            response = transactionTemplate.execute(status -> {
                var registrationRequest = registrationRequestStatusService.createPending(
                        new RegistrationRequest(
                                clientId,
                                request.clientRequestId(),
                                request.registrationNumber(),
                                request.name()
                        )
                );
                return RegistrationRequestResponse.fromNew(registrationRequest);
            });
        } catch (DataIntegrityViolationException ex) {
            var recovered = findExistingByClientRequestId(clientId, request);
            if (recovered != null) {
                return recovered;
            }
            throw ex;
        }

        registrationTaskExecutor.execute(() -> registrationRequestWorker.process(response.requestId()));
        return response;
    }

    private RegistrationRequestResponse findExistingByClientRequestId(
            String clientId,
            RegisterCompanyRequest request
    ) {
        return registrationRequestRepository.findByClientIdAndClientRequestId(clientId, request.clientRequestId())
                .map(existing -> {
                    if (!existing.matchesPayload(request.registrationNumber(), request.name())) {
                        throw new InvalidRegistrationRequestException(
                                "Client request id '%s' was already used with a different registration payload"
                                        .formatted(request.clientRequestId())
                        );
                    }
                    return RegistrationRequestResponse.duplicate(
                            existing,
                            "Duplicate request",
                            resolveCompany(existing)
                    );
                })
                .orElse(null);
    }

    /**
     * Loads status when {@code requestId} belongs to {@code clientId}.
     *
     * @param requestId registration request id
     * @param clientId  owning client
     * @return current request status and payload fields
     */
    @Transactional(readOnly = true)
    public RegistrationRequestStatusResponse getStatus(Long requestId, String clientId) {
        var request = registrationRequestRepository.findByIdAndClientId(requestId, clientId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        return toStatusResponse(request);
    }

    private RegistrationRequestStatusResponse toStatusResponse(RegistrationRequest request) {
        return new RegistrationRequestStatusResponse(
                request.getId(),
                request.getClientRequestId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCompanyName(),
                request.getErrorMessage()
        );
    }

    private CompanyResponse resolveCompany(RegistrationRequest request) {
        if (request.getCompanyId() != null) {
            return companyRepository.findById(request.getCompanyId())
                    .map(CompanyResponse::from)
                    .orElse(null);
        }

        if (request.getStatus() == RequestStatus.COMPLETED) {
            return companyRepository.findByRegistrationNumber(request.getRegistrationNumber())
                    .map(CompanyResponse::from)
                    .orElse(null);
        }

        return null;
    }

}
