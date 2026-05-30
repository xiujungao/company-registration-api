package com.jackie.companyregistration;

import com.jackie.companyregistration.support.SyncAsyncTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SyncAsyncTestConfig.class)
class CompanyRegistrationApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
