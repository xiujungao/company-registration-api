package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clients")
public class ApiClient {

    @Id
    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    protected ApiClient() {
    }

    public ApiClient(String clientId, String apiKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

}
