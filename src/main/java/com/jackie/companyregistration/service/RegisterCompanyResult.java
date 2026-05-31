package com.jackie.companyregistration.service;

import com.jackie.companyregistration.dto.CompanyResponse;

/**
 * Outcome of {@link CompanyService#register} inside the registration worker.
 *
 * @param company  current or newly persisted company
 * @param created  {@code true} when a new row was saved or an INACTIVE company was reactivated
 */
public record RegisterCompanyResult(CompanyResponse company, boolean created) {
}
