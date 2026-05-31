package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.CompanyNameHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link CompanyNameHistory} audit rows.
 * <p>
 * Rows are appended by {@link com.jackie.companyregistration.service.CompanyService} on register,
 * reactivate-with-rename, and {@code PUT} name updates.
 */
public interface CompanyNameHistoryRepository extends JpaRepository<CompanyNameHistory, Long> {

    /**
     * @param companyId {@link com.jackie.companyregistration.model.Company} primary key
     * @return name changes for that company, oldest first
     */
    List<CompanyNameHistory> findByCompanyIdOrderByChangedAtAsc(Long companyId);

}
