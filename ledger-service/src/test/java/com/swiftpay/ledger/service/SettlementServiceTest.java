package com.swiftpay.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftpay.ledger.entity.LedgerTransaction;
import com.swiftpay.ledger.entity.UserAccount;
import com.swiftpay.ledger.repository.LedgerTransactionRepository;
import com.swiftpay.ledger.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {
    @Mock
    private LedgerTransactionRepository transactions;
    @Mock
    private UserAccountRepository users;
    @Mock
    private KafkaTemplate<String, String> kafka;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private StringRedisTemplate redis;
    @Mock
    private UserAccount sender;
    @Mock
    private UserAccount receiver;

    private SettlementService service;

    @BeforeEach
    void setUp() {
        service = new SettlementService(transactions, users, kafka, mapper, redis);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void insufficientFundsMarksTransactionFailedAndPublishesFailure() throws Exception {
        SettlementService.PaymentEvent event = event("tx-insufficient", new BigDecimal("20.00"));
        when(transactions.findByTransactionId(event.transactionId())).thenReturn(Optional.empty());
        when(users.lockAll(List.of("sender", "receiver"))).thenReturn(List.of(sender, receiver));
        when(sender.getId()).thenReturn("sender");
        when(receiver.getId()).thenReturn("receiver");
        when(sender.getBalance()).thenReturn(new BigDecimal("10.00"));
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        service.settle(event);
        runAfterCommit();

        assertEquals("FAILED", savedTransaction().getStatus());
        verify(sender, never()).debit(any());
        verify(receiver, never()).credit(any());
        verify(kafka).send("swiftpay.payment.failed", "tx-insufficient", "{}");
    }

    @Test
    void sufficientFundsCompletesTransactionAndTransfersAmount() throws Exception {
        SettlementService.PaymentEvent event = event("tx-success", new BigDecimal("20.00"));
        when(transactions.findByTransactionId(event.transactionId())).thenReturn(Optional.empty());
        when(users.lockAll(List.of("sender", "receiver"))).thenReturn(List.of(sender, receiver));
        when(sender.getId()).thenReturn("sender");
        when(receiver.getId()).thenReturn("receiver");
        when(sender.getBalance()).thenReturn(new BigDecimal("50.00"));
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        service.settle(event);
        runAfterCommit();

        assertEquals("COMPLETED", savedTransaction().getStatus());
        verify(sender).debit(new BigDecimal("20.00"));
        verify(receiver).credit(new BigDecimal("20.00"));
        verify(kafka).send("swiftpay.payment.completed", "tx-success", "{}");
        verify(redis).delete(List.of("swiftpay:balance:sender", "swiftpay:balance:receiver"));
    }

    @Test
    void completedDuplicateIsIgnoredWithoutRelockingAccounts() {
        SettlementService.PaymentEvent event = event("tx-duplicate", new BigDecimal("20.00"));
        LedgerTransaction existing = new LedgerTransaction("tx-duplicate", "sender", "receiver",
                new BigDecimal("20.00"), "USD", "COMPLETED", Instant.now());
        when(transactions.findByTransactionId(event.transactionId())).thenReturn(Optional.of(existing));

        service.settle(event);

        verify(users, never()).lockAll(any());
        verify(transactions, never()).save(any());
    }

    private SettlementService.PaymentEvent event(String transactionId, BigDecimal amount) {
        return new SettlementService.PaymentEvent("event-1", "original-1", transactionId, "sender", "receiver",
                amount, "USD", "PENDING", Instant.parse("2026-01-01T00:00:00Z"));
    }

    private LedgerTransaction savedTransaction() {
        var captor = org.mockito.ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(transactions).save(captor.capture());
        return captor.getValue();
    }

    private void runAfterCommit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
    }
}