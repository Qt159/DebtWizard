package com.tuan.debtwizard;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFullFlow() throws Exception {
        // 1. Register
        String registerJson = """
            {
              "username": "testuser",
              "password": "123456",
              "fullName": "Test User",
              "email": "test@example.com",
              "monthlyIncome": 20000000
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 2. Login
        String loginJson = """
            {
              "username": "testuser",
              "password": "123456"
            }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        // lấy session cookie
        String sessionCookie = loginResult.getResponse().getHeader("Set-Cookie");
        assertNotNull(sessionCookie, "Login did not return a session cookie");
        // 3. Create Debt
        String debtJson = """
            {
              "debt": {
                "lenderName": "ACB Bank",
                "totalPrincipal": 100000000,
                "startDate": "2024-01-01",
                "termMonths": 24,
                "dueDay": 15,
                "debtType": "INSTALLMENT"
              },
              "interestConfig": {
                "interestCalculationMethod": "FLAT",
                "interestRatePeriod": "MONTHLY",
                "interestRate": 12.0,
                "paymentAllocationRule": "INTEREST_FIRST",
                "gracePeriodDays": 0,
                "lateFee": 50000
              }
            }
        """;

        MvcResult debtResult = mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(debtJson)
                        .header("Cookie", sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lenderName").value("ACB Bank"))
                .andReturn();

        Long debtId = JsonPath.read(debtResult.getResponse().getContentAsString(), "$.id");

        // 4. Create Payment
        String paymentJson = """
            {
              "debtId": %d,
              "amount": 5000000,
              "paymentMethod": "BANK_TRANSFER",
              "paymentDate": "2024-02-15",
              "note": "Trả kỳ 1"
            }
        """.formatted(debtId);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson)
                        .header("Cookie", sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000000));

        // 5. Analysis
        mockMvc.perform(get("/api/analysis/{userId}", 1L)
                        .header("Cookie", sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dti").exists())
                .andExpect(jsonPath("$.interestRatio").exists())
                .andExpect(jsonPath("$.overdue").exists())
                .andExpect(jsonPath("$.repaymentTime").exists());
    }
}
