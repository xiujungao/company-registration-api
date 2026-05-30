package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;
import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.dto.RegistrationRequestResponse;
import com.jackie.companyregistration.dto.RegistrationRequestStatusResponse;
import com.jackie.companyregistration.exception.RequestNotFoundException;
import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import com.jackie.companyregistration.repository.CompanyRepository;
import com.jackie.companyregistration.repository.RegistrationRequestRepository;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RegistrationRequestService {

    private static final EnumSet<RequestStatus> ACTIVE_OR_COMPLETED = EnumSet.of(
            RequestStatus.PENDING,
            RequestStatus.PROCESSING,
            RequestStatus.COMPLETED
    );

    private final RegistrationRequestRepository registrationRequestRepository;
    private final CompanyRepository companyRepository;
    private final RegistrationRequestWorker registrationRequestWorker;
    private final Executor registrationTaskExecutor;
    private final TransactionTemplate transactionTemplate;

    public RegistrationRequestService(
            RegistrationRequestRepository registrationRequestRepository,
            CompanyRepository companyRepository,
            RegistrationRequestWorker registrationRequestWorker,
            @Qualifier("registrationTaskExecutor") Executor registrationTaskExecutor,
            PlatformTransactionManager transactionManager
    ) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.companyRepository = companyRepository;
        this.registrationRequestWorker = registrationRequestWorker;
        this.registrationTaskExecutor = registrationTaskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public RegistrationRequestResponse submit(RegisterCompanyRequest request, String clientId) {
        var duplicateResponse = findDuplicateResponse(request, clientId);
        if (duplicateResponse != null) {
            return duplicateResponse;
        }

        var response = transactionTemplate.execute(status -> {
            var registrationRequest = registrationRequestRepository.save(
                    new RegistrationRequest(clientId, request.registrationNumber(), request.name())
            );
            return RegistrationRequestResponse.fromNew(registrationRequest);
        });

        registrationTaskExecutor.execute(() -> registrationRequestWorker.process(response.requestId()));
        return response;
    }

    private RegistrationRequestResponse findDuplicateResponse(RegisterCompanyRequest request, String clientId) {
        var existingCompany = companyRepository.findByRegistrationNumber(request.registrationNumber());
        if (existingCompany.isPresent()) {
            return duplicateForExistingCompany(existingCompany.get(), request, clientId);
        }

        return registrationRequestRepository
                .findFirstByClientIdAndRegistrationNumberAndCompanyNameAndStatusInOrderByCreatedAtDesc(
                        clientId,
                        request.registrationNumber(),
                        request.name(),
                        ACTIVE_OR_COMPLETED
                )
                .map(existingRequest -> RegistrationRequestResponse.duplicate(
                        existingRequest,
                        "Registration request already submitted",
                        resolveCompany(existingRequest)
                ))
                .orElse(null);
    }

    private RegistrationRequestResponse duplicateForExistingCompany(
            Company company,
            RegisterCompanyRequest request,
            String clientId
    ) {
        var companyResponse = CompanyResponse.from(company);
        var linkedRequest = registrationRequestRepository
                .findFirstByClientIdAndRegistrationNumberAndStatusOrderByCreatedAtDesc(
                        clientId,
                        request.registrationNumber(),
                        RequestStatus.COMPLETED
                );

        if (company.getName().equals(request.name())) {
            return RegistrationRequestResponse.duplicateCompany(
                    linkedRequest.map(RegistrationRequest::getId).orElse(null),
                    linkedRequest.map(RegistrationRequest::getCreatedAt).orElse(Instant.now()),
                    request.registrationNumber(),
                    "Company already registered",
                    companyResponse
            );
        }

        return RegistrationRequestResponse.duplicateCompany(
                linkedRequest.map(RegistrationRequest::getId).orElse(null),
                linkedRequest.map(RegistrationRequest::getCreatedAt).orElse(Instant.now()),
                request.registrationNumber(),
                "Company with registration number '%s' is already registered as '%s'"
                        .formatted(request.registrationNumber(), company.getName()),
                companyResponse
        );
    }

    @Transactional(readOnly = true)
    public RegistrationRequestStatusResponse getStatus(Long requestId, String clientId) {
        var request = registrationRequestRepository.findByIdAndClientId(requestId, clientId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        return toStatusResponse(request);
    }

    private RegistrationRequestStatusResponse toStatusResponse(RegistrationRequest request) {
        return new RegistrationRequestStatusResponse(
                request.getId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCompanyName(),
                resolveCompany(request),
                request.getErrorMessage(),
                request.getCreatedAt(),
                request.getUpdatedAt()
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
