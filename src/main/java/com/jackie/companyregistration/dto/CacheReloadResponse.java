package com.jackie.companyregistration.dto;

/**
 * Result of reloading {@link com.jackie.companyregistration.security.ClientCache}.
 *
 * @param clientCount number of API clients loaded from {@code clients}
 */
public record CacheReloadResponse(int clientCount) {

}
