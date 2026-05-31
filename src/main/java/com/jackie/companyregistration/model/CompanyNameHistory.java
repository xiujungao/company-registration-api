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

/**
 * One recorded company display name at a point in time ({@code company_name_history}).
 * <p>
 * Initial registration and each rename via {@link com.jackie.companyregistration.service.CompanyService}
 * append a row. {@link #company} is loaded lazily ({@link FetchType#LAZY}).
 */
@Entity
@Table(name = "company_name_history")
public class CompanyNameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected CompanyNameHistory() {
    }

    /**
     * @param company parent company (FK {@code company_id} set on persist)
     * @param name    name in effect from {@link #changedAt}
     */
    public CompanyNameHistory(Company company, String name) {
        this.company = company;
        this.name = name;
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

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

}
