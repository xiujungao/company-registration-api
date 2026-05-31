package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.security.AdminAuthFilter;
import com.jackie.companyregistration.support.SyncAsyncTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link AdminCacheController} and {@link AdminAuthFilter}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(SyncAsyncTestConfig.class)
class AdminCacheControllerTest {

    private static final String ADMIN_KEY = "test-admin-key";

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verifies a valid admin key reloads the cache and returns the seeded client count.
     */
    @Test
    void reloadClientsReturnsClientCount() throws Exception {
        mockMvc.perform(post("/api/admin/cache/clients/reload")
                        .header(AdminAuthFilter.ADMIN_KEY_HEADER, ADMIN_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientCount").value(3));
    }

    /**
     * Verifies the reload endpoint requires {@link AdminAuthFilter#ADMIN_KEY_HEADER}.
     */
    @Test
    void reloadClientsRequiresAdminKey() throws Exception {
        mockMvc.perform(post("/api/admin/cache/clients/reload"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing admin key"));
    }

    /**
     * Verifies an incorrect admin key is rejected.
     */
    @Test
    void reloadClientsRejectsInvalidAdminKey() throws Exception {
        mockMvc.perform(post("/api/admin/cache/clients/reload")
                        .header(AdminAuthFilter.ADMIN_KEY_HEADER, "wrong-admin-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid admin key"));
    }

}
