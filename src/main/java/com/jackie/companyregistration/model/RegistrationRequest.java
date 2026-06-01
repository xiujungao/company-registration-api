package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Async registration job owned by an API {@code client_id}. Status transitions are tracked in
 * {@code registration_request_status_history}; successful jobs link to {@code companies} via {@code company_id}.
 * <p>
 * {@link #status} is stored as {@code status_code} (FK to {@code registration_request_statuses}); application code
 * uses {@link RequestStatus} only — no lookup repository.
 */
@Entity
@Table(
        name = "registration_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_registration_requests_client_client_request_id",
                columnNames = {"client_id", "client_request_id"}
        )
)
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "client_request_id", nullable = false)
    private String clientRequestId;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false, length = 32)
    private RequestStatus status;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RegistrationRequest() {
    }

    public RegistrationRequest(
            String clientId,
            String clientRequestId,
            String registrationNumber,
            String companyName
    ) {
        this.clientId = clientId;
        this.clientRequestId = clientRequestId;
        this.registrationNumber = registrationNumber;
        this.companyName = companyName;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public boolean matchesPayload(String registrationNumber, String companyName) {
        return this.registrationNumber.equals(registrationNumber)
                && this.companyName.equals(companyName);
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
