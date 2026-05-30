package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.model.CompanyStatus;
import com.jackie.companyregistration.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CompanyDbLookupService {

    private final CompanyRepository companyRepository;

    public CompanyDbLookupService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<CompanyDbLookupResult> lookup(String registrationNumber, String companyName) {
        return Mono.fromCallable(() -> resolve(registrationNumber, companyName))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private CompanyDbLookupResult resolve(String registrationNumber, String companyName) {
        var existingByRegistrationNumber = companyRepository.findByRegistrationNumberAndStatus(
                registrationNumber,
                CompanyStatus.ACTIVE
        );
        if (existingByRegistrationNumber.isPresent()) {
            Company company = existingByRegistrationNumber.get();
            if (company.getName().equals(companyName)) {
                return new CompanyDbLookupResult.LinkExisting(company);
            }
            return new CompanyDbLookupResult.Rejected(
                    "Registration number '%s' is already registered with company name '%s'. Use the update name API to change it."
                            .formatted(registrationNumber, company.getName())
            );
        }

        var existingByName = companyRepository.findByNameAndStatus(companyName, CompanyStatus.ACTIVE);
        if (existingByName.isPresent()) {
            Company company = existingByName.get();
            return new CompanyDbLookupResult.Rejected(
                    "Company name '%s' is already registered with registration number '%s'."
                            .formatted(companyName, company.getRegistrationNumber())
            );
        }

        return new CompanyDbLookupResult.NoMatch();
    }

}
