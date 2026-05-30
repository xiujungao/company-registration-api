package com.jackie.companyregistration.service.lookup;

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

    private final CompanyDbLookupService companyDbLookupService;
    private final VectorSearchService vectorSearchService;

    public RegistrationLookupOrchestrator(
            CompanyDbLookupService companyDbLookupService,
            VectorSearchService vectorSearchService
    ) {
        this.companyDbLookupService = companyDbLookupService;
        this.vectorSearchService = vectorSearchService;
    }

    public LookupOutcome resolve(String registrationNumber, String companyName) {
        return resolveAsync(registrationNumber, companyName).block();
    }

    Mono<LookupOutcome> resolveAsync(String registrationNumber, String companyName) {
        Mono<LookupOutcome> vectorSearch = vectorSearchService.searchByName(companyName)
                .subscribeOn(Schedulers.boundedElastic())
                .map(ignored -> (LookupOutcome) new LookupOutcome.RegisterNew())
                .cache();

        AtomicReference<Disposable> vectorSubscription = new AtomicReference<>();
        vectorSubscription.set(vectorSearch.subscribe(
                ignored -> {},
                error -> log.warn("Vector search failed for '{}'", companyName, error)
        ));

        return companyDbLookupService.lookup(registrationNumber, companyName)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(dbResult -> switch (dbResult) {
                    case CompanyDbLookupResult.LinkExisting link -> {
                        cancelVectorSearch(vectorSubscription, companyName);
                        yield Mono.just(new LookupOutcome.LinkExisting(link.company()));
                    }
                    case CompanyDbLookupResult.Rejected rejected -> {
                        cancelVectorSearch(vectorSubscription, companyName);
                        yield Mono.just(new LookupOutcome.Rejected(rejected.message()));
                    }
                    case CompanyDbLookupResult.NoMatch ignored -> vectorSearch;
                });
    }

    private void cancelVectorSearch(AtomicReference<Disposable> vectorSubscription, String companyName) {
        Disposable subscription = vectorSubscription.getAndSet(null);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.debug("Cancelled vector search for company name '{}'", companyName);
        }
    }

}
