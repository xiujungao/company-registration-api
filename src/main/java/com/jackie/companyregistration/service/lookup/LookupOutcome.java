package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;

public sealed interface LookupOutcome permits
        LookupOutcome.LinkExisting,
        LookupOutcome.Rejected,
        LookupOutcome.RegisterNew {

    record LinkExisting(Company company) implements LookupOutcome {
    }

    record Rejected(String message) implements LookupOutcome {
    }

    record RegisterNew() implements LookupOutcome {
    }

}
