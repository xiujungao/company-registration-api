package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;
import com.jackie.companyregistration.repository.CompanyRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ExactMatchLookupService {

    private final CompanyRepository companyRepository;

    public ExactMatchLookupService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Mono<Optional<Company>> lookup(String registrationNumber) {
        return Mono.fromCallable(() -> companyRepository.findByRegistrationNumber(registrationNumber))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
