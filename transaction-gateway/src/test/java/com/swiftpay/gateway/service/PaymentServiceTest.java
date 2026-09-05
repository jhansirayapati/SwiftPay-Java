package com.swiftpay.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftpay.gateway.dto.PaymentRequest;
import com.swiftpay.gateway.dto.PaymentResponse;
import com.swiftpay.gateway.entity.PaymentTransaction;
import com.swiftpay.gateway.repository.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentTransactionRepository repository;
    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> values;
    @Mock
    private KafkaTemplate<String, String> kafka;
    @Mock
    private ObjectMapper mapper;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
        service = new PaymentService(repository, redis, kafka, mapper);
    }

    @Test
    void repeatedRequestWithSamePayloadReturnsExistingPayment() throws Exception {
        PaymentRequest request = request("tx-1", "coffee");
        PaymentTransaction saved = new PaymentTransaction("tx-1", "sender", "receiver", new BigDecimal("12.50"), "USD");
        when(values.get("swiftpay:idempotency:tx-1")).thenReturn(null);
        when(values.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(repository.saveAndFlush(any(PaymentTransaction.class))).thenReturn(saved);
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        service.initiate(request);

        ArgumentCaptor<String> claimCaptor = ArgumentCaptor.forClass(String.class);
        verify(values).setIfAbsent(anyString(), claimCaptor.capture(), any());
        when(values.get("swiftpay:idempotency:tx-1")).thenReturn(claimCaptor.getValue());
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.of(saved));

        PaymentResponse response = service.initiate(request);

        assertEquals("tx-1", response.transactionId());
        assertEquals("PENDING", response.status());
        verify(repository).findByTransactionId("tx-1");
    }

    @Test
    void reusedTransactionIdWithDifferentPayloadIsRejected() {
        PaymentRequest request = request("tx-1", "tea");
        when(values.get("swiftpay:idempotency:tx-1")).thenReturn("different-hash|PENDING");

        assertThrows(IllegalArgumentException.class, () -> service.initiate(request));

        verifyNoInteractions(repository, kafka, mapper);
    }

    @Test
    void unclaimedRequestCreatesPaymentAndPublishesInitiation() throws Exception {
        PaymentRequest request = request("tx-2", "book");
        when(values.get("swiftpay:idempotency:tx-2")).thenReturn(null);
        when(values.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(repository.saveAndFlush(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        PaymentResponse response = service.initiate(request);

        assertEquals("tx-2", response.transactionId());
        assertEquals("PENDING", response.status());
        verify(repository).saveAndFlush(any(PaymentTransaction.class));
        verify(kafka).send("swiftpay.payment.initiated", "tx-2", "{}");
    }

    private PaymentRequest request(String transactionId, String description) {
        return new PaymentRequest(transactionId, "sender", "receiver", new BigDecimal("12.50"), "USD", description);
    }
}