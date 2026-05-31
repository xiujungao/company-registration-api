package com.jackie.companyregistration.web;

import com.jackie.companyregistration.security.ApiKeyAuthFilter;
import com.jackie.companyregistration.support.SyncAsyncTestConfig;
import com.jackie.companyregistration.support.TestApiKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SyncAsyncTestConfig.class)
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.client-requests-per-minute=2"
})
class RateLimitInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsTooManyRequestsWhenClientLimitExceeded() throws Exception {
        var request = get("/api/companies/requests/1")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, TestApiKeys.DEV_API_KEY)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isNotFound());
        mockMvc.perform(request).andExpect(status().isNotFound());
        mockMvc.perform(request)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded"));
    }

}
