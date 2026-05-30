package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.CompanyNameHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyNameHistoryRepository extends JpaRepository<CompanyNameHistory, Long> {

    List<CompanyNameHistory> findByCompanyIdOrderByChangedAtAsc(Long companyId);

}
