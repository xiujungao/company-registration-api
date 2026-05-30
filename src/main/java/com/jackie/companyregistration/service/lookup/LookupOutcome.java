package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;

public sealed interface LookupOutcome permits LookupOutcome.ExactMatch, LookupOutcome.NoMatch {

    record ExactMatch(Company company, boolean sameName) implements LookupOutcome {
    }

    record NoMatch() implements LookupOutcome {
    }

}
