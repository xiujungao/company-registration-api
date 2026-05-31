package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.CompanyNameHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link CompanyNameHistory} audit rows in {@code company_name_history}.
 * <p>
 * Rows are appended by {@link com.jackie.companyregistration.service.CompanyService} on register,
 * reactivate-with-rename, and {@code PUT} name updates. Only {@link #findByCompanyIdOrderByChangedAtAsc(Long)}
 * is used in application code today; other {@link JpaRepository} methods support tests and ad-hoc queries.
 */
public interface CompanyNameHistoryRepository extends JpaRepository<CompanyNameHistory, Long> {

    /**
     * @param companyId {@link com.jackie.companyregistration.model.Company} primary key
     * @return name changes for that company, oldest first
     */
    List<CompanyNameHistory> findByCompanyIdOrderByChangedAtAsc(Long companyId);

}
