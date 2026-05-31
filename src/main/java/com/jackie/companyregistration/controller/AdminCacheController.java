package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.dto.CacheReloadResponse;
import com.jackie.companyregistration.security.ClientCache;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin operations for in-memory caches.
 * <p>
 * Protected by {@link com.jackie.companyregistration.security.AdminAuthFilter} ({@code X-Admin-Key}).
 * Each application instance holds its own {@link ClientCache}; endpoints here reload only the
 * instance that receives the request. With multiple pods, call each pod (or use scheduled refresh
 * on every instance) so all caches pick up {@code clients} table changes.
 */
@RestController
@RequestMapping("/api/admin/cache")
public class AdminCacheController {

    private final ClientCache clientCache;

    /**
     * @param clientCache in-memory API key index for this JVM
     */
    public AdminCacheController(ClientCache clientCache) {
        this.clientCache = clientCache;
    }

    /**
     * Reloads the client cache from the {@code clients} table on this instance.
     *
     * @return number of clients loaded into memory on this JVM
     */
    @PostMapping("/clients/reload")
    public CacheReloadResponse reloadClients() {
        return new CacheReloadResponse(clientCache.reload());
    }

}
