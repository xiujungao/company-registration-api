package com.jackie.companyregistration.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs duration and records Micrometer timers for all {@code @Service} methods under
 * {@code com.jackie.companyregistration.service}.
 */
@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);
    private static final String METRIC_NAME = "service.method";
    private static final double[] PERCENTILES = {0.5, 0.95, 0.99};

    private final MeterRegistry meterRegistry;

    public ServiceLoggingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.jackie.companyregistration.service..*(..))")
    public Object logServiceCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = joinPoint.getSignature();
        var methodLabel = signature.toShortString();
        var className = signature.getDeclaringType().getSimpleName();
        var methodName = signature.getName();

        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            var durationMs = System.currentTimeMillis() - start;
            var exceptionTag = error == null ? "none" : error.getClass().getSimpleName();

            serviceTimer(className, methodName, exceptionTag).record(durationMs, TimeUnit.MILLISECONDS);

            if (error != null) {
                log.info("{} failed in {} ms (error={})",
                        methodLabel, durationMs, exceptionTag);
            } else {
                log.info("{} completed in {} ms", methodLabel, durationMs);
            }
        }
    }

    private Timer serviceTimer(String className, String methodName, String exceptionTag) {
        return Timer.builder(METRIC_NAME)
                .tags("class", className, "method", methodName, "exception", exceptionTag)
                .publishPercentiles(PERCENTILES)
                .register(meterRegistry);
    }

}
