package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "registration_request_statuses")
public class RegistrationRequestStatusEntity {

    @Id
    @Column(length = 32)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean terminal;

    protected RegistrationRequestStatusEntity() {
    }

    public RegistrationRequestStatusEntity(String code, String displayName, int sortOrder, boolean terminal) {
        this.code = code;
        this.displayName = displayName;
        this.sortOrder = sortOrder;
        this.terminal = terminal;
    }

    public static RegistrationRequestStatusEntity from(RequestStatus status) {
        return new RegistrationRequestStatusEntity(
                status.name(),
                status.getDisplayName(),
                status.getSortOrder(),
                status.isTerminal()
        );
    }

    public void syncFrom(RequestStatus status) {
        this.displayName = status.getDisplayName();
        this.sortOrder = status.getSortOrder();
        this.terminal = status.isTerminal();
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isTerminal() {
        return terminal;
    }

}
