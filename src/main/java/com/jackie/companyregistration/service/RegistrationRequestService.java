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
 * Entry point for async company registration from {@link com.jackie.companyregistration.controller.CompanyController}.
 * <p>
 * {@link #submit} accepts a validated {@link RegisterCompanyRequest} and authenticated {@code clientId}
 * (from {@link com.jackie.companyregistration.security.ApiKeyAuthFilter} via
 * {@link com.jackie.companyregistration.security.ClientContext}). It either:
 * <ul>
 *   <li>returns an existing row when {@code clientId + clientRequestId} matches the same payload ({@code duplicate=true}), or</li>
 *   <li>persist a new {@link RequestStatus#PENDING} row and enqueue {@link RegistrationRequestWorker#process(long)}
 *       on {@code registrationTaskExecutor} ({@code duplicate=false}, HTTP 202).</li>
 * </ul>
 * Reusing a {@code clientRequestId} with a different registration number or name throws
 * {@link InvalidRegistrationRequestException}. Duplicate company checks are deferred to the worker, not submit.
 * <p>
 * {@link #getStatus} returns the current worker outcome for a {@code requestId} scoped to the owning client.
 * Concurrent submits for the same idempotency key rely on the DB unique constraint
 * ({@code client_id}, {@code client_request_id}) with {@link DataIntegrityViolationException} recovery in
 * {@link #submit}.
 *
 * @see RegistrationRequestWorker
 * @see RegistrationRequestStatusService
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
     * @param registrationRequestRepository   registration request persistence
     * @param companyRepository               used to populate {@code company} on duplicate replays when known
     * @param registrationRequestWorker       background lookup and register logic
     * @param registrationRequestStatusService creates pending rows and status history
     * @param registrationTaskExecutor        background pool from {@link com.jackie.companyregistration.config.AsyncConfig}
     * @param transactionManager              used to build a {@link TransactionTemplate} for submit persistence
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
     * <p>
     * Persistence runs in a short {@link TransactionTemplate} transaction; worker execution is fire-and-forget
     * after commit. Does not perform registration-number or company-name duplicate validation (worker only).
     *
     * @param request  payload including {@code clientRequestId}
     * @param clientId authenticated client from {@link com.jackie.companyregistration.security.ClientContext}
     * @return new request ({@code duplicate=false}) or prior match ({@code duplicate=true})
     * @throws InvalidRegistrationRequestException when {@code clientRequestId} exists with a different payload
     */
    public RegistrationRequestResponse submit(RegisterCompanyRequest request, String clientId) {
        // Idempotency check
        // Same clientRequestId: matching payload → return existing row (HTTP 200);
        // different payload → throw InvalidRegistrationRequestException.
        var existingByClientRequestId = findExistingByClientRequestId(clientId, request);
        if (existingByClientRequestId != null) {
            return existingByClientRequestId;
        }

        // Persist the new PENDING row
        RegistrationRequestResponse response;
        try {
            // TransactionTemplate (not @Transactional on submit): commit the PENDING insert here so the
            // worker enqueued below sees the row; @Transactional on submit would still be open at enqueue time.
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
            // Concurrent submits with the same clientId + clientRequestId: both passed the read above,
            // both tried INSERT; DB unique key uk_registration_requests_client_client_request_id rejects the loser.
            var recovered = findExistingByClientRequestId(clientId, request);
            if (recovered != null) {
                return recovered;
            }
            throw ex;
        }

        // Fire-and-forget worker enqueue: commit the PENDING insert above so the worker sees the row;
        // @Transactional on submit would still be open at enqueue time.
        registrationTaskExecutor.execute(() -> registrationRequestWorker.process(response.requestId()));

        return response;
    }

    /**
     * Resolves an idempotent replay for {@code clientId + clientRequestId}, or {@code null} when no row exists.
     *
     * @param clientId owning client
     * @param request  submitted payload
     * @return duplicate response when the row exists and matches; {@code null} otherwise
     * @throws InvalidRegistrationRequestException when the id exists but reg# or name differ
     */
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
     * @param requestId registration request primary key from submit
     * @param clientId  owning client (from API key)
     * @return current request status and payload fields
     * @throws RequestNotFoundException when the id is unknown or owned by another client
     */
    @Transactional(readOnly = true)
    public RegistrationRequestStatusResponse getStatus(Long requestId, String clientId) {
        var request = registrationRequestRepository.findByIdAndClientId(requestId, clientId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        return toStatusResponse(request);
    }

    /** Maps a {@link RegistrationRequest} entity to the public status DTO (no status history). */
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

    /**
     * Best-effort company snapshot for duplicate replays: linked {@code companyId}, or lookup by reg# when
     * {@link RequestStatus#COMPLETED} without a stored company id.
     *
     * @param request existing registration request row
     * @return company DTO or {@code null} when not yet resolvable
     */
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
