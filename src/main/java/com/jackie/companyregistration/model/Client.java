package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Client() {
    }

    public Client(String clientId, String apiKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
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

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
