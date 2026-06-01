package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;

/**
 * Result of synchronous duplicate checks against {@code companies} (ACTIVE rows only).
 * Mapped to {@link LookupOutcome} by {@link RegistrationLookupOrchestrator}.
 */
public sealed interface CompanyDbLookupResult permits
        CompanyDbLookupResult.LinkExisting,
        CompanyDbLookupResult.Rejected,
        CompanyDbLookupResult.NoMatch {

    /** Same registration number and name as an existing ACTIVE company. */
    record LinkExisting(Company company) implements CompanyDbLookupResult {
    }

    /** Reg# or name conflicts with an existing ACTIVE company. */
    record Rejected(String message) implements CompanyDbLookupResult {
    }

    /** No ACTIVE row matches registration number or name — safe to register (subject to vector search). */
    record NoMatch() implements CompanyDbLookupResult {
    }

}
