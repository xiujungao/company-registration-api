package com.jackie.companyregistration.security;

import com.jackie.companyregistration.config.AdminProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that authenticates {@code /api/admin/*} requests via {@code X-Admin-Key}.
 * <p>
 * Registered by {@link com.jackie.companyregistration.config.AdminAuthConfig}. Client API keys
 * ({@link ApiKeyAuthFilter}) are not accepted on admin routes. If {@link AdminProperties#apiKey()}
 * is not configured, admin endpoints return {@code 503}.
 */
public class AdminAuthFilter extends OncePerRequestFilter {

    /** HTTP header carrying the admin shared secret. */
    public static final String ADMIN_KEY_HEADER = "X-Admin-Key";

    private final AdminProperties adminProperties;

    /**
     * @param adminProperties configuration holding the expected admin key
     */
    public AdminAuthFilter(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    /**
     * Validates {@link #ADMIN_KEY_HEADER} against {@link AdminProperties#apiKey()} and either
     * continues the filter chain or writes an error response.
     *
     * @param request     incoming HTTP request
     * @param response    HTTP response used for error bodies when auth fails
     * @param filterChain remaining servlet filters and the dispatcher servlet
     * @throws ServletException if the filter chain fails
     * @throws IOException      if writing an error response fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var configuredKey = adminProperties.apiKey();
        if (configuredKey == null || configuredKey.isBlank()) {
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE, "Admin API is not configured");
            return;
        }

        var providedKey = request.getHeader(ADMIN_KEY_HEADER);
        if (providedKey == null || providedKey.isBlank()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing admin key");
            return;
        }

        if (!keysMatch(providedKey, configuredKey)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Invalid admin key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Constant-time comparison of the provided and configured admin keys.
     *
     * @param provided value from {@link #ADMIN_KEY_HEADER}
     * @param stored   configured {@link AdminProperties#apiKey()}
     * @return {@code true} if the keys match
     */
    private static boolean keysMatch(String provided, String stored) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Writes a JSON error body and stops filter processing.
     *
     * @param response servlet response to write to
     * @param status   HTTP status to return
     * @param message  short error text included in the JSON {@code message} field
     * @throws IOException if the response body cannot be written
     */
    private static void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

}
