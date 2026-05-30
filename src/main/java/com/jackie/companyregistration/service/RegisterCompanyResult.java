package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;

public record RegisterCompanyResult(CompanyResponse company, boolean created) {
}
