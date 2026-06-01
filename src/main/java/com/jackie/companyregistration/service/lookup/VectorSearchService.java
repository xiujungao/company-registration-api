package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Placeholder for future vector similarity search by company name.
 * <p>
 * {@link #findSimilarCompany} simulates latency ({@code app.vector-search.delay-ms}) and currently always
 * returns empty. Failures are logged by {@link RegistrationLookupOrchestrator} and do not fail registration
 * when the DB lookup returns {@link CompanyDbLookupResult.NoMatch}.
 */
@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final Duration searchDelay;

    /**
     * @param delayMs simulated search latency inside {@link #findSimilarCompany}
     *                ({@code app.vector-search.delay-ms}, default 1000)
     */
    public VectorSearchService(
            @Value("${app.vector-search.delay-ms:1000}") long delayMs
    ) {
        this.searchDelay = Duration.ofMillis(delayMs);
    }

    /**
     * Returns a lazy mono for vector similarity by company name; execution starts when
     * {@link RegistrationLookupOrchestrator} subscribes (not when this method returns).
     *
     * @param companyName name to search for similar ACTIVE companies
     * @return matching company when implemented; currently always {@link Optional#empty()}
     */
    public Mono<Optional<Company>> searchByName(String companyName) {
        return Mono
                // Lazy: findSimilarCompany runs only when a downstream subscriber subscribes
                .fromCallable(() -> findSimilarCompany(companyName))
                // When subscribed, run that callable on Reactor's boundedElastic pool (blocking-safe)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Optional<Company> findSimilarCompany(String companyName) {
        log.info(
                "Vector search starting for '{}' on thread {} (delay {} ms)",
                companyName,
                Thread.currentThread().getName(),
                searchDelay.toMillis()
        );
        if (!searchDelay.isZero()) {
            try {
                Thread.sleep(searchDelay.toMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Vector search interrupted for '" + companyName + "'", ex);
            }
        }
        // Placeholder: vector similarity search will be implemented later.
        var result = Optional.<Company>empty();
        log.info(
                "Vector search finished for '{}' on thread {} (match={})",
                companyName,
                Thread.currentThread().getName(),
                result.isPresent()
        );
        return result;
    }

}
