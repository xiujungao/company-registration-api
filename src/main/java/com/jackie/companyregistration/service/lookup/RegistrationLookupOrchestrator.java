package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class RegistrationLookupOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(RegistrationLookupOrchestrator.class);

    private final ExactMatchLookupService exactMatchLookupService;
    private final VectorSearchService vectorSearchService;

    public RegistrationLookupOrchestrator(
            ExactMatchLookupService exactMatchLookupService,
            VectorSearchService vectorSearchService
    ) {
        this.exactMatchLookupService = exactMatchLookupService;
        this.vectorSearchService = vectorSearchService;
    }

    public LookupOutcome resolve(String registrationNumber, String companyName) {
        return resolveAsync(registrationNumber, companyName).block();
    }

    Mono<LookupOutcome> resolveAsync(String registrationNumber, String companyName) {
        Mono<Optional<Company>> vectorSearch = vectorSearchService.searchByName(companyName)
                .subscribeOn(Schedulers.boundedElastic())
                .cache();

        AtomicReference<Disposable> vectorSubscription = new AtomicReference<>();
        vectorSubscription.set(vectorSearch.subscribe(
                ignored -> {},
                error -> log.warn("Vector search failed for '{}'", companyName, error)
        ));

        return exactMatchLookupService.lookup(registrationNumber)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exactMatch -> {
                    if (exactMatch.isPresent()) {
                        cancelVectorSearch(vectorSubscription, registrationNumber);
                        Company company = exactMatch.get();
                        return Mono.just(new LookupOutcome.ExactMatch(
                                company,
                                company.getName().equals(companyName)
                        ));
                    }

                    return vectorSearch.map(vectorMatch -> toOutcomeAfterVectorSearch(vectorMatch, companyName));
                });
    }

    private LookupOutcome toOutcomeAfterVectorSearch(Optional<Company> vectorMatch, String companyName) {
        if (vectorMatch.isPresent()) {
            Company company = vectorMatch.get();
            return new LookupOutcome.ExactMatch(company, company.getName().equals(companyName));
        }
        return new LookupOutcome.NoMatch();
    }

    private void cancelVectorSearch(AtomicReference<Disposable> vectorSubscription, String registrationNumber) {
        Disposable subscription = vectorSubscription.getAndSet(null);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.debug("Cancelled vector search for registration number '{}'", registrationNumber);
        }
    }

}
