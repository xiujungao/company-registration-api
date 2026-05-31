package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for {@link Company} rows. Supports registration duplicate checks and name updates.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /** @param registrationNumber unique business key */
    Optional<Company> findByRegistrationNumber(String registrationNumber);

    /** @param registrationNumber unique business key */
    Optional<Company> findByRegistrationNumberAndStatus(String registrationNumber, CompanyStatus status);

    /** Active-company lookup by display name (duplicate name detection). */
    Optional<Company> findByNameAndStatus(String name, CompanyStatus status);

    /**
     * Whether another active company already uses {@code name} under a different registration number.
     */
    boolean existsByNameAndStatusAndRegistrationNumberNot(
            String name,
            CompanyStatus status,
            String registrationNumber
    );

}
