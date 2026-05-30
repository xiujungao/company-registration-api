package com.jackie.companyregistration.security;

import jakarta.servlet.http.HttpServletRequest;

public final class ApiClientContext {

    public static final String CLIENT_ID_ATTRIBUTE = "authenticatedClientId";

    private ApiClientContext() {
    }

    public static void setClientId(HttpServletRequest request, String clientId) {
        request.setAttribute(CLIENT_ID_ATTRIBUTE, clientId);
    }

    public static String getClientId(HttpServletRequest request) {
        return (String) request.getAttribute(CLIENT_ID_ATTRIBUTE);
    }

}
