package com.jackie.companyregistration.service.lookup;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * Runs DB and vector-search duplicate checks before {@link com.jackie.companyregistration.service.CompanyService#register}.
 * <p>
 * Vector search starts in parallel with the DB lookup. When the DB returns {@link CompanyDbLookupResult.LinkExisting}
 * or {@link CompanyDbLookupResult.Rejected}, the vector subscription is cancelled. On
 * {@link CompanyDbLookupResult.NoMatch}, the vector result is used (today always {@link LookupOutcome.RegisterNew}).
 * Vector failures are logged and do not fail the request when the DB path allows registration.
 * <p>
 * Blocking work is scheduled in {@link CompanyDbLookupService} and {@link VectorSearchService} via
 * {@code subscribeOn(Schedulers.boundedElastic())}; this class only composes the reactive pipeline.
 */
@Service
public class RegistrationLookupOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(RegistrationLookupOrchestrator.class);

    private final CompanyDbLookupService companyDbLookupService;
    private final VectorSearchService vectorSearchService;

    /**
     * @param companyDbLookupService authoritative ACTIVE company duplicate rules
     * @param vectorSearchService    parallel similarity search (placeholder)
     */
    public RegistrationLookupOrchestrator(
            CompanyDbLookupService companyDbLookupService,
            VectorSearchService vectorSearchService
    ) {
        this.companyDbLookupService = companyDbLookupService;
        this.vectorSearchService = vectorSearchService;
    }

    /**
     * Blocking entry point used by {@link com.jackie.companyregistration.service.RegistrationRequestWorker}.
     * <p>
     * Subscribes to {@link #resolveAsync} and waits on the worker thread via {@link Mono#block()}.
     *
     * @param registrationNumber submitted registration number
     * @param companyName        submitted display name
     * @return worker action: link existing, reject, or register new
     */
    public LookupOutcome resolve(String registrationNumber, String companyName) {
        // block(): subscribe to resolveAsync, wait on worker thread, unwrap Mono<LookupOutcome> → LookupOutcome
        return resolveAsync(registrationNumber, companyName).block();
    }

    /**
     * Reactive pipeline: DB lookup and vector search in parallel (each offloaded in its service).
     *
     * @param registrationNumber submitted registration number
     * @param companyName        submitted display name
     * @return mono that completes with the final {@link LookupOutcome}
     */
    Mono<LookupOutcome> resolveAsync(String registrationNumber, String companyName) {
        // Lazy mono only — findSimilarCompany runs when subscribe() below starts it
        Mono<LookupOutcome> vectorSearch = vectorSearchService.searchByName(companyName)
                .map(ignored -> (LookupOutcome) new LookupOutcome.RegisterNew())
                .cache();

        AtomicReference<Disposable> vectorSubscription = new AtomicReference<>();
        // Fire vector search in parallel with DB lookup; returns immediately (non-blocking)
        vectorSubscription.set(vectorSearch.subscribe(
                ignored -> {},
                error -> log.warn("Vector search failed for '{}'", companyName, error)
        ));

        // DB lookup (lazy until block/subscribe); flatMap picks outcome — DB wins on link/reject
        return companyDbLookupService.lookup(registrationNumber, companyName)
                .flatMap(dbResult -> switch (dbResult) {
                    // Existing ACTIVE company matches reg# + name — skip vector, link request
                    case CompanyDbLookupResult.LinkExisting link -> {
                        cancelVectorSearch(vectorSubscription, companyName);
                        yield Mono.just(new LookupOutcome.LinkExisting(link.company()));
                    }
                    // Reg# or name conflict — skip vector, fail request
                    case CompanyDbLookupResult.Rejected rejected -> {
                        cancelVectorSearch(vectorSubscription, companyName);
                        yield Mono.just(new LookupOutcome.Rejected(rejected.message()));
                    }
                    // No DB duplicate — wait for cached vectorSearch (today → RegisterNew)
                    case CompanyDbLookupResult.NoMatch _ -> vectorSearch;
                });
    }

    /**
     * Disposes the early {@code vectorSearch.subscribe()} when DB lookup already returned
     * {@link CompanyDbLookupResult.LinkExisting} or {@link CompanyDbLookupResult.Rejected},
     * so {@link VectorSearchService#findSimilarCompany} is not left running unnecessarily.
     *
     * @param vectorSubscription handle from {@link #resolveAsync}; cleared after dispose
     * @param companyName        used for debug logging only
     */
    private void cancelVectorSearch(AtomicReference<Disposable> vectorSubscription, String companyName) {
        Disposable subscription = vectorSubscription.getAndSet(null);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose(); // signals Reactor to cancel upstream (may interrupt Thread.sleep)
            log.debug("Cancelled vector search for company name '{}'", companyName);
        }
    }

}
