package com.swiftpay.gateway.dto;

import java.time.Instant;

public record PaymentResponse(String transactionId, String status, String senderId, String receiverId,
                              String amount, String currency, Instant createdAt) { }