package com.jackie.companyregistration.dto;

import com.jackie.companyregistration.model.RequestStatus;
import java.time.Instant;

public record RegistrationRequestStatusHistoryEntry(
        RequestStatus status,
        Instant changedAt,
        String errorMessage
) {
}
