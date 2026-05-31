package com.jackie.companyregistration.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Request-scoped holder for the authenticated client ID after API key validation.
 * <p>
 * {@link ApiKeyAuthFilter} calls {@link #setClientId(HttpServletRequest, String)} on success;
 * controllers read it with {@link #getClientId(HttpServletRequest)}. The value is stored as a
 * servlet request attribute (not static state), so each HTTP request has its own client ID.
 * <p>
 * Without this helper, each caller would invoke {@code request.setAttribute(...)} and
 * {@code request.getAttribute(...)} with a string key directly. That scatters the same magic
 * string ({@value #CLIENT_ID_ATTRIBUTE}) across the filter and every controller, so a typo
 * or rename in one place would silently break auth context (writer sets {@code "authenticatedClientId"},
 * reader asks for {@code "clientId"} → {@code null}). Centralizing the key in
 * {@link #CLIENT_ID_ATTRIBUTE} and access in {@link #setClientId} / {@link #getClientId} keeps
 * producers and consumers aligned and gives one place to change the attribute name.
 */
public final class ClientContext {

    /** Request attribute name for the resolved {@code client_id}. */
    public static final String CLIENT_ID_ATTRIBUTE = "authenticatedClientId";

    /** Prevents instantiation; use static helpers only. */
    private ClientContext() {
    }

    /**
     * Records the authenticated client for this request.
     *
     * @param request  current HTTP request
     * @param clientId value from the {@code clients} table
     */
    public static void setClientId(HttpServletRequest request, String clientId) {
        request.setAttribute(CLIENT_ID_ATTRIBUTE, clientId);
    }

    /**
     * Returns the client ID set by {@link ApiKeyAuthFilter}, or {@code null} if unset.
     *
     * @param request current HTTP request
     * @return authenticated {@code client_id}, or {@code null}
     */
    public static String getClientId(HttpServletRequest request) {
        return (String) request.getAttribute(CLIENT_ID_ATTRIBUTE);
    }

}
