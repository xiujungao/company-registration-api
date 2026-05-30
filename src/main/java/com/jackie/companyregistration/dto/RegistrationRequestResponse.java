package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.RegistrationRequest;
import com.jackie.companyregistration.model.RequestStatus;
import java.time.Instant;

public record RegistrationRequestResponse(
        Long requestId,
        RequestStatus status,
        String registrationNumber,
        Instant createdAt,
        boolean duplicate,
        String message,
        CompanyResponse company
) {

    public static RegistrationRequestResponse fromNew(RegistrationRequest request) {
        return new RegistrationRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCreatedAt(),
                false,
                null,
                null
        );
    }

    public static RegistrationRequestResponse duplicate(
            RegistrationRequest request,
            String message,
            CompanyResponse company
    ) {
        return new RegistrationRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getRegistrationNumber(),
                request.getCreatedAt(),
                true,
                message,
                company
        );
    }

    public static RegistrationRequestResponse duplicateCompany(
            Long requestId,
            Instant createdAt,
            String registrationNumber,
            String message,
            CompanyResponse company
    ) {
        return new RegistrationRequestResponse(
                requestId,
                RequestStatus.COMPLETED,
                registrationNumber,
                createdAt,
                true,
                message,
                company
        );
    }

}
