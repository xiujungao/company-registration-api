package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Append-only audit row for a {@link RequestStatus} change on {@link RegistrationRequest}.
 * Written by {@link com.jackie.companyregistration.service.RegistrationRequestStatusService}; not exposed on the public API.
 */
@Entity
@Table(name = "registration_request_status_history")
public class RegistrationRequestStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_request_id", nullable = false)
    private RegistrationRequest registrationRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false, length = 32)
    private RequestStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected RegistrationRequestStatusHistory() {
    }

    /**
     * @param registrationRequest parent request (saved)
     * @param status              new status at this point in time
     * @param errorMessage        failure detail for {@link RequestStatus#FAILED}; {@code null} otherwise
     */
    public RegistrationRequestStatusHistory(
            RegistrationRequest registrationRequest,
            RequestStatus status,
            String errorMessage
    ) {
        this.registrationRequest = registrationRequest;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public RegistrationRequest getRegistrationRequest() {
        return registrationRequest;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

}
