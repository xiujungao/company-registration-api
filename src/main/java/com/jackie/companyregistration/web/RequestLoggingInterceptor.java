package com.jackie.companyregistration.web;

import com.jackie.companyregistration.security.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Logs API request start and completion with status and duration.
 * <p>
 * Runs after servlet auth filters, so {@link ClientContext} is available on client routes.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    /** Request attribute holding {@link System#currentTimeMillis()} at the start of handling. */
    static final String START_TIME_ATTRIBUTE = "requestStartTimeMs";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        var clientId = ClientContext.getClientId(request);
        if (clientId != null) {
            log.info("{} {} clientId={}", request.getMethod(), request.getRequestURI(), clientId);
        } else {
            log.info("{} {}", request.getMethod(), request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        var start = request.getAttribute(START_TIME_ATTRIBUTE);
        if (!(start instanceof Long startTime)) {
            return;
        }

        var durationMs = System.currentTimeMillis() - startTime;
        if (ex != null) {
            log.info("{} {} -> {} ({} ms, error={})",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    ex.getClass().getSimpleName());
            return;
        }

        log.info("{} {} -> {} ({} ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs);
    }

}
