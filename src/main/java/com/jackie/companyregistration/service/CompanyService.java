package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;
import com.jackie.companyregistration.dto.RegisterCompanyRequest;
import com.jackie.companyregistration.exception.DuplicateCompanyException;
import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(noRollbackFor = DuplicateCompanyException.class)
    public RegisterCompanyResult register(RegisterCompanyRequest request) {
        var existing = companyRepository.findByRegistrationNumber(request.registrationNumber());
        if (existing.isPresent()) {
            return handleExisting(existing.get(), request);
        }

        var company = companyRepository.save(
                new Company(request.registrationNumber(), request.name())
        );
        return new RegisterCompanyResult(CompanyResponse.from(company), true);
    }

    private RegisterCompanyResult handleExisting(Company existing, RegisterCompanyRequest request) {
        if (!existing.getName().equals(request.name())) {
            throw new DuplicateCompanyException(request.registrationNumber());
        }
        return new RegisterCompanyResult(CompanyResponse.from(existing), false);
    }

}
