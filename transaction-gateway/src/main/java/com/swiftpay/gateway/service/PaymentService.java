package com.swiftpay.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.swiftpay.gateway.dto.*;
import com.swiftpay.gateway.entity.PaymentTransaction;
import com.swiftpay.gateway.repository.PaymentTransactionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentTransactionRepository repository; private final StringRedisTemplate redis; private final KafkaTemplate<String,String> kafka; private final ObjectMapper mapper;
    public PaymentService(PaymentTransactionRepository repository, StringRedisTemplate redis, KafkaTemplate<String,String> kafka, ObjectMapper mapper) { this.repository=repository;this.redis=redis;this.kafka=kafka;this.mapper=mapper; }
    public PaymentResponse initiate(PaymentRequest request) {
        String payload = canonical(request); String hash = sha256(payload); String key="swiftpay:idempotency:"+request.transactionId();
        String cached = redis.opsForValue().get(key);
        if (cached != null) { if (!cached.startsWith(hash+"|")) throw new IllegalArgumentException("Transaction ID already used with a different payload"); return response(repository.findByTransactionId(request.transactionId()).orElseThrow()); }
        Boolean claimed = redis.opsForValue().setIfAbsent(key, hash+"|PENDING", java.time.Duration.ofHours(24));
        if (Boolean.FALSE.equals(claimed)) { PaymentTransaction existing=repository.findByTransactionId(request.transactionId()).orElseThrow(() -> new IllegalStateException("Payment is being created")); if (!hash.equals(redis.opsForValue().get(key).split("\\|",2)[0])) throw new IllegalArgumentException("Transaction ID already used with a different payload"); return response(existing); }
        PaymentTransaction payment=new PaymentTransaction(request.transactionId(),request.senderId(),request.receiverId(),request.amount(),request.currency());
        try { payment=repository.saveAndFlush(payment); kafka.send("swiftpay.payment.initiated", payment.getTransactionId(), mapper.writeValueAsString(new PaymentEvent(payment))); return response(payment); }
        catch (DataIntegrityViolationException ex) { return response(repository.findByTransactionId(request.transactionId()).orElseThrow()); }
        catch (Exception ex) { throw new IllegalStateException("Unable to publish payment", ex); }
    }
    private String canonical(PaymentRequest r){return String.join("|",r.transactionId(),r.senderId(),r.receiverId(),r.amount().toPlainString(),r.currency(),r.description()==null?"":r.description());}
    private String sha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private PaymentResponse response(PaymentTransaction p){return new PaymentResponse(p.getTransactionId(),p.getStatus(),p.getSenderId(),p.getReceiverId(),p.getAmount().toPlainString(),p.getCurrency(),p.getCreatedAt());}
    record PaymentEvent(String eventId,String originalEventId,String transactionId,String senderId,String receiverId,
                        @JsonSerialize(using=ToStringSerializer.class) java.math.BigDecimal amount,
                        String currency,String status,java.time.Instant timestamp) {
        PaymentEvent(PaymentTransaction p){this(java.util.UUID.randomUUID().toString(),p.getTransactionId(),p.getTransactionId(),p.getSenderId(),p.getReceiverId(),p.getAmount(),p.getCurrency(),p.getStatus(),p.getCreatedAt());}
    }
}