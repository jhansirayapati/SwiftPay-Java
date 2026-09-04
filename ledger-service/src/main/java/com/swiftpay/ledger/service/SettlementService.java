package com.swiftpay.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.swiftpay.ledger.entity.LedgerTransaction;
import com.swiftpay.ledger.entity.UserAccount;
import com.swiftpay.ledger.repository.LedgerTransactionRepository;
import com.swiftpay.ledger.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SettlementService {
	private final LedgerTransactionRepository tx;
	private final UserAccountRepository users;
	private final KafkaTemplate<String, String> kafka;
	private final ObjectMapper mapper;
	private final StringRedisTemplate redis;

	public SettlementService(LedgerTransactionRepository t, UserAccountRepository u, KafkaTemplate<String, String> k,
							 ObjectMapper m, StringRedisTemplate r) {
		tx = t; users = u; kafka = k; mapper = m; redis = r;
	}

	@Transactional
	public void settle(PaymentEvent e) {
		Optional<LedgerTransaction> old = tx.findByTransactionId(e.transactionId());
		if (old.isPresent()) {
			if ("PENDING".equals(old.get().getStatus())) process(old.get(), e);
			return;
		}
		LedgerTransaction transaction = new LedgerTransaction(e.transactionId(), e.senderId(), e.receiverId(), e.amount(),
				e.currency(), "PENDING", e.timestamp() == null ? Instant.now() : e.timestamp());
		tx.save(transaction);
		process(transaction, e);
	}

	private void process(LedgerTransaction transaction, PaymentEvent e) {
		List<UserAccount> locked = users.lockAll(List.of(e.senderId(), e.receiverId()));
		Map<String, UserAccount> byId = locked.stream().collect(Collectors.toMap(UserAccount::getId, x -> x));
		UserAccount sender = byId.get(e.senderId());
		UserAccount receiver = byId.get(e.receiverId());
		if (sender == null || receiver == null || sender.getBalance().compareTo(e.amount()) < 0) {
			transaction.fail();
			publishAfterCommit("swiftpay.payment.failed", transaction, e, "Insufficient funds or account not found");
			return;
		}
		sender.debit(e.amount());
		receiver.credit(e.amount());
		transaction.complete();
		publishAfterCommit("swiftpay.payment.completed", transaction, e, null);
	}

	private void publishAfterCommit(String topic, LedgerTransaction transaction, PaymentEvent source, String reason) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override public void afterCommit() {
				try {
					kafka.send(topic, transaction.getTransactionId(), mapper.writeValueAsString(
							new TerminalEvent(UUID.randomUUID().toString(), source.eventId(), transaction.getTransactionId(),
									source.senderId(), source.receiverId(), transaction.getAmount(), transaction.getCurrency(),
									transaction.getStatus(), reason, Instant.now())));
				} catch (Exception ignored) { }
				try {
					redis.delete(List.of("swiftpay:balance:" + source.senderId(), "swiftpay:balance:" + source.receiverId()));
				} catch (Exception ignored) { }
			}
		});
	}

	public record PaymentEvent(String eventId, String originalEventId, String transactionId, String senderId,
							   String receiverId, BigDecimal amount, String currency, String status, Instant timestamp) { }

	public record TerminalEvent(String eventId, String originalEventId, String transactionId, String senderId,
								String receiverId, @JsonSerialize(using = ToStringSerializer.class) BigDecimal amount,
								String currency, String status, String reason, Instant timestamp) { }
}