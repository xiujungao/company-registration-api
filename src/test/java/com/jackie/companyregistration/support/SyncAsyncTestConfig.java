package com.jackie.companyregistration.support;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

/**
 * Runs registration worker logic synchronously on the test thread instead of
 * {@link com.jackie.companyregistration.config.AsyncConfig}.
 */
@TestConfiguration
@Profile("test")
public class SyncAsyncTestConfig {

    /**
     * @return same-bean-name executor that executes tasks inline for deterministic tests
     */
    @Bean(name = "registrationTaskExecutor")
    Executor registrationTaskExecutor() {
        return new SyncTaskExecutor();
    }

}
