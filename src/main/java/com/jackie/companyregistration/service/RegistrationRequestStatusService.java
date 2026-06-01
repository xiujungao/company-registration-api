package com.jackie.companyregistration.service;

import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RegistrationRequestStatusHistory;
import com.jackie.companyregistration.model.RequestStatus;
import com.jackie.companyregistration.repository.RegistrationRequestRepository;
import com.jackie.companyregistration.repository.RegistrationRequestStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists registration request status changes and appends rows to {@code registration_request_status_history}.
 * Status values are {@link RequestStatus} enum constants; {@code registration_request_statuses} is reference data
 * seeded via {@code db/ddl/data.sql} for foreign-key integrity only.
 */
@Service
public class RegistrationRequestStatusService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final RegistrationRequestStatusHistoryRepository historyRepository;

    /**
     * @param registrationRequestRepository persists {@link RegistrationRequest} status updates
     * @param historyRepository             append-only transition log
     */
    public RegistrationRequestStatusService(
            RegistrationRequestRepository registrationRequestRepository,
            RegistrationRequestStatusHistoryRepository historyRepository
    ) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.historyRepository = historyRepository;
    }

    /** Creates a new request in {@link RequestStatus#PENDING} and records the first history entry. */
    @Transactional
    public RegistrationRequest createPending(RegistrationRequest request) {
        return transition(request, RequestStatus.PENDING, null);
    }

    /**
     * Updates request status (and optional error message) and appends a history row.
     *
     * @param request      request entity to update
     * @param status       new status
     * @param errorMessage failure detail for {@code FAILED}; {@code null} otherwise
     * @return saved request
     */
    @Transactional
    public RegistrationRequest transition(RegistrationRequest request, RequestStatus status, String errorMessage) {
        request.setStatus(status);
        request.setErrorMessage(errorMessage);
        var saved = registrationRequestRepository.save(request);
        historyRepository.save(new RegistrationRequestStatusHistory(saved, status, errorMessage));
        return saved;
    }

}
