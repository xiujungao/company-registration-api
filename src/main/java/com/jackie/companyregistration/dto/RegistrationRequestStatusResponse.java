package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.RequestStatus;
import java.time.Instant;
import java.util.List;

public record RegistrationRequestStatusResponse(
        Long requestId,
        String clientRequestId,
        RequestStatus status,
        String registrationNumber,
        String companyName,
        CompanyResponse company,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        List<RegistrationRequestStatusHistoryEntry> statusHistory
) {
}
