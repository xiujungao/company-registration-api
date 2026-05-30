package com.jackie.companyregistration.service.lookup;

import com.jackie.companyregistration.model.Company;

public sealed interface CompanyDbLookupResult permits
        CompanyDbLookupResult.LinkExisting,
        CompanyDbLookupResult.Rejected,
        CompanyDbLookupResult.NoMatch {

    record LinkExisting(Company company) implements CompanyDbLookupResult {
    }

    record Rejected(String message) implements CompanyDbLookupResult {
    }

    record NoMatch() implements CompanyDbLookupResult {
    }

}
