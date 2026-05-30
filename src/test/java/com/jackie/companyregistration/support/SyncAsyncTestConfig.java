package com.jackie.companyregistration.support;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

@TestConfiguration
@Profile("test")
public class SyncAsyncTestConfig {

    @Bean(name = "registrationTaskExecutor")
    Executor registrationTaskExecutor() {
        return new SyncTaskExecutor();
    }

}
