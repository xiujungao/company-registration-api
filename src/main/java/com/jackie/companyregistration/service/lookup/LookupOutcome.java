package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;

/**
 * Result of duplicate lookup before {@link com.jackie.companyregistration.service.CompanyService#register}.
 * Produced by {@link RegistrationLookupOrchestrator}; consumed by
 * {@link com.jackie.companyregistration.service.RegistrationRequestWorker}.
 */
public sealed interface LookupOutcome permits
        LookupOutcome.LinkExisting,
        LookupOutcome.Rejected,
        LookupOutcome.RegisterNew {

    /** An ACTIVE company already matches registration number and name — link, do not insert. */
    record LinkExisting(Company company) implements LookupOutcome {
    }

    /** Registration blocked (e.g. reg# or name conflict); message is stored on the request as {@code FAILED}. */
    record Rejected(String message) implements LookupOutcome {
    }

    /** No blocking duplicate — proceed to create a new company row. */
    record RegisterNew() implements LookupOutcome {
    }

}
