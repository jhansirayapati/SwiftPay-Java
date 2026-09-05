package com.swiftpay.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.swiftpay.gateway.dto.PaymentResponse;
import com.swiftpay.gateway.service.PaymentService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PaymentService paymentService;

    @Test
    void acceptsSnakeCasePaymentAndReturnsPending() throws Exception {
        when(paymentService.initiate(any())).thenReturn(new PaymentResponse(
                "txn-web-1", "PENDING", "user_001", "user_002", "10.00", "INR", Instant.parse("2026-01-01T00:00:00Z")));

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transaction_id\":\"txn-web-1\",\"sender_id\":\"user_001\",\"receiver_id\":\"user_002\",\"amount\":\"10.00\",\"currency\":\"INR\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value("txn-web-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void rejectsMissingRequiredPaymentFields() throws Exception {
        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transaction_id\":\"txn-web-2\",\"amount\":\"10.00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
