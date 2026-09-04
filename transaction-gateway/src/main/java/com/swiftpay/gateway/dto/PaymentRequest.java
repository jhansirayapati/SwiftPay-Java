package com.swiftpay.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(@JsonProperty("transaction_id") @NotBlank String transactionId,
                             @JsonProperty("sender_id") @NotBlank String senderId,
                             @JsonProperty("receiver_id") @NotBlank String receiverId,
                             @JsonProperty("amount") @NotNull @DecimalMin("0.01") BigDecimal amount,
                             @JsonProperty("currency") @NotBlank String currency,
                             String description) { }