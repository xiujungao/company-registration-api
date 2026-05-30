package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class VectorSearchService {

    private final Duration searchDelay;

    public VectorSearchService(
            @Value("${app.vector-search.delay-ms:1000}") long delayMs
    ) {
        this.searchDelay = Duration.ofMillis(delayMs);
    }

    public Mono<Optional<Company>> searchByName(String companyName) {
        return Mono.delay(searchDelay)
                .then(Mono.fromCallable(() -> findSimilarCompany(companyName)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Optional<Company> findSimilarCompany(String companyName) {
        // Placeholder: vector similarity search will be implemented later.
        return Optional.empty();
    }

}
