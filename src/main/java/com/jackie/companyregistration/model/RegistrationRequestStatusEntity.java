package com.jackie.companyregistration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps {@code registration_request_statuses} for DDL and {@code db/ddl/data.sql} seed data only.
 * Application code uses {@link RequestStatus}; do not load or persist rows via JPA.
 */
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

}
