package com.jackie.companyregistration.service;

import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RegistrationRequestStatusHistory;
import com.jackie.companyregistration.model.RequestStatus;
import com.jackie.companyregistration.repository.RegistrationRequestRepository;
import com.jackie.companyregistration.repository.RegistrationRequestStatusHistoryRepository;
import com.jackie.companyregistration.repository.RegistrationRequestStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationRequestStatusService {

    private final RegistrationRequestStatusRepository registrationRequestStatusRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final RegistrationRequestStatusHistoryRepository historyRepository;

    public RegistrationRequestStatusService(
            RegistrationRequestStatusRepository registrationRequestStatusRepository,
            RegistrationRequestRepository registrationRequestRepository,
            RegistrationRequestStatusHistoryRepository historyRepository
    ) {
        this.registrationRequestStatusRepository = registrationRequestStatusRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public RegistrationRequest createPending(RegistrationRequest request) {
        return transition(request, RequestStatus.PENDING, null);
    }

    @Transactional
    public RegistrationRequest transition(RegistrationRequest request, RequestStatus status, String errorMessage) {
        request.setStatusEntity(registrationRequestStatusRepository.getReferenceById(status.name()));
        request.setErrorMessage(errorMessage);
        var saved = registrationRequestRepository.save(request);
        historyRepository.save(new RegistrationRequestStatusHistory(
                saved,
                registrationRequestStatusRepository.getReferenceById(status.name()),
                errorMessage
        ));
        return saved;
    }

}
