package com.jackie.companyregistration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that authenticates {@code /api/*} requests via the {@code X-API-Key} header.
 * <p>
 * Registered by {@link com.jackie.companyregistration.config.ApiKeyAuthConfig} for {@code /api/*}
 * at the highest filter order. {@code /api/admin/*} is skipped in
 * {@link #shouldNotFilter(HttpServletRequest)} so {@link com.jackie.companyregistration.config.AdminAuthConfig}
 * handles admin auth.
 * <p>
 * On success, the resolved {@code client_id} is stored on the request through
 * {@link ClientContext} for controllers to read. On failure, responds with {@code 401} and a
 * small JSON body without invoking downstream handlers.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    /** HTTP header carrying the client's API key. */
    public static final String API_KEY_HEADER = "X-API-Key";

    private final ClientCache clientCache;

    /**
     * Creates the filter with the shared API key cache used for authentication lookups.
     *
     * @param clientCache in-memory index of valid API keys, loaded at application startup
     */
    public ApiKeyAuthFilter(ClientCache clientCache) {
        this.clientCache = clientCache;
    }

    /**
     * Skips client API-key auth for {@code /api/admin/*} (handled by {@link AdminAuthFilter}).
     *
     * @param request incoming HTTP request
     * @return {@code true} if the filter should not run for this request
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/admin/");
    }

    /**
     * Authenticates the request and either continues the filter chain or aborts with {@code 401}.
     * <p>
     * Reads {@link #API_KEY_HEADER}. If absent or blank, responds with {@code "Missing API key"}.
     * If present but unknown, responds with {@code "Invalid API key"}. On success, sets
     * {@link ClientContext} and calls {@link FilterChain#doFilter(jakarta.servlet.ServletRequest,
     * jakarta.servlet.ServletResponse)}.
     *
     * @param request     incoming HTTP request
     * @param response    HTTP response used for {@code 401} bodies when auth fails
     * @param filterChain remaining servlet filters and the dispatcher servlet
     * @throws ServletException if the filter chain fails
     * @throws IOException      if writing the unauthorized response fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var providedKey = request.getHeader(API_KEY_HEADER);
        if (providedKey == null || providedKey.isBlank()) {
            writeUnauthorized(response, "Missing API key");
            return;
        }

        var clientId = clientCache.findClientIdByApiKey(providedKey).orElse(null);
        if (clientId == null) {
            writeUnauthorized(response, "Invalid API key");
            return;
        }

        ClientContext.setClientId(request, clientId);
        filterChain.doFilter(request, response);
    }

    /**
     * Writes a {@code 401 Unauthorized} JSON error and does not proceed down the filter chain.
     *
     * @param response servlet response to write to
     * @param message  short error text included in the JSON {@code message} field
     * @throws IOException if the response body cannot be written
     */
    private static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

}
