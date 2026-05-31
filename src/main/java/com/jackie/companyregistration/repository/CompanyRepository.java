package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link Company} rows in {@code companies}.
 * <p>
 * Used by {@link com.jackie.companyregistration.service.CompanyService} and
 * {@link com.jackie.companyregistration.service.lookup.CompanyDbLookupService} for registration,
 * rename, and ACTIVE-only duplicate checks ({@code uk_companies_name_active}).
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * @param registrationNumber unique business key
     * @return company regardless of {@link CompanyStatus}
     */
    Optional<Company> findByRegistrationNumber(String registrationNumber);

    /**
     * @param registrationNumber unique business key
     * @param status             lifecycle filter (typically {@link CompanyStatus#ACTIVE})
     * @return matching row, or empty
     */
    Optional<Company> findByRegistrationNumberAndStatus(String registrationNumber, CompanyStatus status);

    /**
     * Active-company lookup by display name (duplicate name detection).
     *
     * @param name   company display name
     * @param status usually {@link CompanyStatus#ACTIVE}
     */
    Optional<Company> findByNameAndStatus(String name, CompanyStatus status);

    /**
     * Whether another company with {@code status} already uses {@code name} under a different
     * registration number.
     *
     * @param registrationNumber row to exclude (the company being renamed or reactivated)
     */
    boolean existsByNameAndStatusAndRegistrationNumberNot(
            String name,
            CompanyStatus status,
            String registrationNumber
    );

}
