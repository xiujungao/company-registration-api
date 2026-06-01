package com.jackie.companyregistration.controller;

import com.jackie.companyregistration.support.TestApiKeys;
import com.jackie.companyregistration.model.CompanyStatus;
import com.jackie.companyregistration.repository.CompanyNameHistoryRepository;
import com.jackie.companyregistration.repository.CompanyRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SyncAsyncTestConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyNameHistoryRepository companyNameHistoryRepository;

    private static final String API_KEY = TestApiKeys.DEV_API_KEY;

    @Test
    void submitRegistrationReturnsAccepted() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-001",
                                  "registrationNumber": "REG-001",
                                  "name": "Acme Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.clientRequestId").value("client-req-001"))
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
                                  "clientRequestId": "client-req-010",
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
                .andExpect(jsonPath("$.clientRequestId").value("client-req-010"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-010"))
                .andExpect(jsonPath("$.companyName").value("Async Corp"));
    }

    @Test
    void sameClientRequestIdReturnsSameInternalRequestId() throws Exception {
        var body = """
                {
                  "clientRequestId": "client-req-double-click",
                  "registrationNumber": "REG-100",
                  "name": "Double Click Corp"
                }
                """;

        var first = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andReturn();

        var firstRequestId = ((Number) com.jayway.jsonpath.JsonPath.read(
                first.getResponse().getContentAsString(),
                "$.requestId"
        )).longValue();

        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.clientRequestId").value("client-req-double-click"))
                .andExpect(jsonPath("$.requestId").value(firstRequestId));
    }

    @Test
    void sameClientRequestIdWithDifferentPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-mismatch",
                                  "registrationNumber": "REG-060",
                                  "name": "Original Payload"
                                }
                                """))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-mismatch",
                                  "registrationNumber": "REG-061",
                                  "name": "Different Payload"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Client request id 'client-req-mismatch' was already used with a different registration payload"));
    }

    @Test
    void resubmitSameRegistrationCompletesViaWorker() throws Exception {
        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clientRequestId": "client-req-020-a",
                          "registrationNumber": "REG-020",
                          "name": "Idempotent Corp"
                        }
                        """))
                .andExpect(status().isAccepted());

        var secondResult = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-020-b",
                                  "registrationNumber": "REG-020",
                                  "name": "Idempotent Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        var secondRequestId = com.jayway.jsonpath.JsonPath.read(
                secondResult.getResponse().getContentAsString(),
                "$.requestId"
        );

        mockMvc.perform(get("/api/companies/requests/{requestId}", secondRequestId)
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-020"))
                .andExpect(jsonPath("$.companyName").value("Idempotent Corp"));
    }

    @Test
    void resubmitDifferentRegistrationNumberForExistingNameFailsViaWorker() throws Exception {
        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clientRequestId": "client-req-040-a",
                          "registrationNumber": "REG-040",
                          "name": "Shared Name Corp"
                        }
                        """))
                .andExpect(status().isAccepted());

        var submitResult = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-040-b",
                                  "registrationNumber": "REG-041",
                                  "name": "Shared Name Corp"
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
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value(
                        "Company name 'Shared Name Corp' is already registered with registration number 'REG-040'."));
    }

    @Test
    void resubmitDifferentNameFailsViaWorker() throws Exception {
        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clientRequestId": "client-req-011-a",
                          "registrationNumber": "REG-011",
                          "name": "Original Name"
                        }
                        """))
                .andExpect(status().isAccepted());

        var submitResult = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-011-b",
                                  "registrationNumber": "REG-011",
                                  "name": "Different Name"
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
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value(
                        "Registration number 'REG-011' is already registered with company name 'Original Name'. Use the update name API to change it."));
    }

    @Test
    void inactiveCompanyNameCanBeReusedByNewRegistration() throws Exception {
        var firstSubmit = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-050-a",
                                  "registrationNumber": "REG-050",
                                  "name": "Reuse Me Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        var firstRequestId = com.jayway.jsonpath.JsonPath.read(
                firstSubmit.getResponse().getContentAsString(),
                "$.requestId"
        );

        mockMvc.perform(get("/api/companies/requests/{requestId}", firstRequestId)
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        var inactiveCompany = companyRepository.findByRegistrationNumber("REG-050").orElseThrow();
        inactiveCompany.setStatus(CompanyStatus.INACTIVE);
        companyRepository.save(inactiveCompany);

        var secondSubmit = mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-050-b",
                                  "registrationNumber": "REG-051",
                                  "name": "Reuse Me Corp"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        var secondRequestId = com.jayway.jsonpath.JsonPath.read(
                secondSubmit.getResponse().getContentAsString(),
                "$.requestId"
        );

        mockMvc.perform(get("/api/companies/requests/{requestId}", secondRequestId)
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.registrationNumber").value("REG-051"))
                .andExpect(jsonPath("$.companyName").value("Reuse Me Corp"));
    }

    @Test
    void updateCompanyNameReturnsUpdatedCompany() throws Exception {
        mockMvc.perform(post("/api/companies")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "clientRequestId": "client-req-030",
                          "registrationNumber": "REG-030",
                          "name": "Old Name Corp"
                        }
                        """))
                .andExpect(status().isAccepted());

        mockMvc.perform(put("/api/companies/{registrationNumber}", "REG-030")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Name Corp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("REG-030"))
                .andExpect(jsonPath("$.name").value("New Name Corp"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        var company = companyRepository.findByRegistrationNumber("REG-030").orElseThrow();
        var history = companyNameHistoryRepository.findByCompanyIdOrderByChangedAtAsc(company.getId());
        org.assertj.core.api.Assertions.assertThat(history).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(history.get(0).getName()).isEqualTo("Old Name Corp");
        org.assertj.core.api.Assertions.assertThat(history.get(1).getName()).isEqualTo("New Name Corp");
    }

    @Test
    void updateNameRejectsInactiveCompany() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "client-req-031",
                                  "registrationNumber": "REG-031",
                                  "name": "Inactive Rename Corp"
                                }
                                """))
                .andExpect(status().isAccepted());

        var company = companyRepository.findByRegistrationNumber("REG-031").orElseThrow();
        company.setStatus(CompanyStatus.INACTIVE);
        companyRepository.save(company);

        mockMvc.perform(put("/api/companies/{registrationNumber}", "REG-031")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Should Not Apply"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("inactive")));

        org.assertj.core.api.Assertions.assertThat(companyRepository.findByRegistrationNumber("REG-031")
                .orElseThrow()
                .getName()).isEqualTo("Inactive Rename Corp");
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
                                  "clientRequestId": "client-req-003",
                                  "registrationNumber": "REG-003",
                                  "name": "No Key Corp"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing API key"));
    }

    @Test
    void blankRegistrationFieldsReturnAllValidationErrors() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientRequestId": "",
                                  "registrationNumber": "",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.clientRequestId").value("Client request id is required"))
                .andExpect(jsonPath("$.errors.registrationNumber").value("Registration number is required"))
                .andExpect(jsonPath("$.errors.name").value("Company name is required"));
    }

}
