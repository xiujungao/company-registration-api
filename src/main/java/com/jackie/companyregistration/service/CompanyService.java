package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;
import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.exception.CompanyInactiveException;
import com.jackie.companyregistration.exception.CompanyNotFoundException;
import com.jackie.companyregistration.exception.DuplicateCompanyException;
import com.jackie.companyregistration.exception.DuplicateCompanyNameException;
import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyNameHistory;
import com.jackie.companyregistration.model.CompanyStatus;
import com.jackie.companyregistration.repository.CompanyNameHistoryRepository;
import com.jackie.companyregistration.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Synchronous company persistence: register (from the async worker), reactivate, and rename.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyNameHistoryRepository companyNameHistoryRepository;

    /**
     * @param companyNameHistoryRepository appends rows on create and rename
     */
    public CompanyService(
            CompanyRepository companyRepository,
            CompanyNameHistoryRepository companyNameHistoryRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyNameHistoryRepository = companyNameHistoryRepository;
    }

    /**
     * Creates or matches a company for a registration worker. Conflicts throw {@link DuplicateCompanyException}.
     */
    @Transactional(noRollbackFor = DuplicateCompanyException.class)
    public RegisterCompanyResult register(RegisterCompanyRequest request) {
        var existing = companyRepository.findByRegistrationNumber(request.registrationNumber());
        if (existing.isPresent()) {
            var company = existing.get();
            if (company.getStatus() == CompanyStatus.INACTIVE) {
                return reactivate(company, request);
            }
            return handleExisting(company, request);
        }

        var company = companyRepository.save(
                new Company(request.registrationNumber(), request.name())
        );
        companyNameHistoryRepository.save(new CompanyNameHistory(company, request.name()));
        return new RegisterCompanyResult(CompanyResponse.from(company), true);
    }

    /**
     * Renames an active company and records {@code company_name_history}.
     *
     * @param registrationNumber company key
     * @param newName            new display name
     * @return updated company
     */
    @Transactional
    public CompanyResponse updateName(String registrationNumber, String newName) {
        Company company = companyRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new CompanyNotFoundException(registrationNumber));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new CompanyInactiveException(registrationNumber);
        }

        if (company.getName().equals(newName)) {
            return CompanyResponse.from(company);
        }

        if (companyRepository.existsByNameAndStatusAndRegistrationNumberNot(
                newName,
                CompanyStatus.ACTIVE,
                registrationNumber
        )) {
            throw new DuplicateCompanyNameException(newName);
        }

        company.setName(newName);
        var saved = companyRepository.save(company);
        companyNameHistoryRepository.save(new CompanyNameHistory(saved, newName));
        return CompanyResponse.from(saved);
    }

    private RegisterCompanyResult handleExisting(Company existing, RegisterCompanyRequest request) {
        if (!existing.getName().equals(request.name())) {
            throw new DuplicateCompanyException(request.registrationNumber());
        }
        return new RegisterCompanyResult(CompanyResponse.from(existing), false);
    }

    private RegisterCompanyResult reactivate(Company company, RegisterCompanyRequest request) {
        if (!company.getName().equals(request.name())) {
            if (companyRepository.existsByNameAndStatusAndRegistrationNumberNot(
                    request.name(),
                    CompanyStatus.ACTIVE,
                    company.getRegistrationNumber()
            )) {
                throw new DuplicateCompanyNameException(request.name());
            }
            company.setName(request.name());
            companyNameHistoryRepository.save(new CompanyNameHistory(company, request.name()));
        }
        company.setStatus(CompanyStatus.ACTIVE);
        return new RegisterCompanyResult(CompanyResponse.from(companyRepository.save(company)), true);
    }

}
