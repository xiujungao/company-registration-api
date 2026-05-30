package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "registration_request_status_history")
public class RegistrationRequestStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_request_id", nullable = false)
    private RegistrationRequest registrationRequest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code", nullable = false)
    private RegistrationRequestStatusEntity status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected RegistrationRequestStatusHistory() {
    }

    public RegistrationRequestStatusHistory(
            RegistrationRequest registrationRequest,
            RegistrationRequestStatusEntity status,
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

    public RegistrationRequestStatusEntity getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

}
