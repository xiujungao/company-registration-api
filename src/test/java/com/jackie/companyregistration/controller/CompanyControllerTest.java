package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.config.ClientSeeder;
import com.jackie.companyregistration.security.ApiKeyAuthFilter;
import com.jackie.companyregistration.support.SyncAsyncTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SyncAsyncTestConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String API_KEY = ClientSeeder.DEV_API_KEY;

    @Test
    void submitRegistrationReturnsAccepted() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registrationNumber": "REG-001",
                                  "name": "Acme Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.duplicate").value(false))
                .andExpect(jsonPath("$.registrationNumber").value("REG-001"));
    }

    @Test
    void getStatusReturnsCompletedAfterProcessing() throws Exception {
        var submitResult = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registrationNumber": "REG-010",
                                  "name": "Async Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        var requestId = com.jayway.jsonpath.JsonPath.read(
                submitResult.getResponse().getContentAsString(),
                "$.requestId"
        );

        mockMvc.perform(get("/api/companies/requests/{requestId}", requestId)
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-010"))
                .andExpect(jsonPath("$.company.id").exists())
                .andExpect(jsonPath("$.company.name").value("Async Corp"));
    }

    @Test
    void resubmitSameRegistrationReturnsDuplicateWithCompany() throws Exception {
        var body = """
                {
                  "registrationNumber": "REG-020",
                  "name": "Idempotent Corp"
                }
                """;

        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Company already registered"))
                .andExpect(jsonPath("$.company.registrationNumber").value("REG-020"))
                .andExpect(jsonPath("$.company.name").value("Idempotent Corp"));
    }

    @Test
    void resubmitDifferentNameReturnsDuplicateWithExistingCompany() throws Exception {
        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "registrationNumber": "REG-011",
                          "name": "Original Name"
                        }
                        """))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registrationNumber": "REG-011",
                                  "name": "Different Name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value(
                        "Company with registration number 'REG-011' is already registered as 'Original Name'"))
                .andExpect(jsonPath("$.company.name").value("Original Name"));
    }

    @Test
    void getStatusReturnsNotFoundForUnknownRequest() throws Exception {
        mockMvc.perform(get("/api/companies/requests/{requestId}", 99999L)
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingApiKeyReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registrationNumber": "REG-003",
                                  "name": "No Key Corp"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing API key"));
    }

    @Test
    void invalidApiKeyReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registrationNumber": "REG-004",
                                  "name": "Bad Key Corp"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid API key"));
    }

}
