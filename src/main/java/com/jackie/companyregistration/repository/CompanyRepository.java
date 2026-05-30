package com.jackie.companyregistration.repository;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByRegistrationNumber(String registrationNumber);

    Optional<Company> findByRegistrationNumberAndStatus(String registrationNumber, CompanyStatus status);

    Optional<Company> findByNameAndStatus(String name, CompanyStatus status);

    boolean existsByNameAndStatusAndRegistrationNumberNot(
            String name,
            CompanyStatus status,
            String registrationNumber
    );

}
