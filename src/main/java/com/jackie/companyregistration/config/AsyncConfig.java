package com.jackie.companyregistration.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Platform thread pool for asynchronous registration processing after HTTP {@code 202 Accepted}.
 * <p>
 * {@link com.jackie.companyregistration.service.RegistrationRequestService#submit} enqueues
 * {@link com.jackie.companyregistration.service.RegistrationRequestWorker#process(long)} here so
 * Tomcat threads are not held for vector search and duplicate checks. Concurrency is capped at
 * {@code maxPoolSize} (8) with a bounded queue (100); this is independent of Tomcat virtual-thread
 * settings and should stay aligned with {@code spring.datasource.hikari.maximum-pool-size}.
 * <p>
 * Tests replace this bean with {@link com.jackie.companyregistration.support.SyncAsyncTestConfig}
 * ({@link org.springframework.core.task.SyncTaskExecutor}) so work runs on the calling thread.
 */
@Configuration
public class AsyncConfig {

    /**
     * Executor for background registration jobs ({@code registration-*} thread names).
     *
     * @return bounded {@link ThreadPoolTaskExecutor} (core 2, max 8, queue 100)
     */
    @Bean(name = "registrationTaskExecutor")
    @Profile("!test")
    Executor registrationTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("registration-");
        executor.initialize();
        return executor;
    }

}
