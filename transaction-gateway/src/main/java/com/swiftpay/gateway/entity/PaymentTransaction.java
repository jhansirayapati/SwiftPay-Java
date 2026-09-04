package com.swiftpay.gateway.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(name = "uk_transactions_transaction_id", columnNames = "transaction_id"),
       indexes = @Index(name = "idx_transactions_payer_created", columnList = "payer_id,created_at"))
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="transaction_id", nullable=false, length=80) private String transactionId;
    @Column(name="payer_id", nullable=false, length=80) private String senderId;
    @Column(name="payee_id", nullable=false, length=80) private String receiverId;
    @Column(nullable=false, precision=19, scale=4) private BigDecimal amount;
    @Column(nullable=false, length=3) private String currency;
    @Column(nullable=false, length=16) private String status;
    @Column(nullable=false) private Instant createdAt;
    protected PaymentTransaction() { }
    public PaymentTransaction(PaymentTransaction p) { transactionId=p.transactionId; senderId=p.senderId; receiverId=p.receiverId; amount=p.amount; currency=p.currency; status=p.status; createdAt=p.createdAt; }
    public PaymentTransaction(String id,String sender,String receiver,BigDecimal amount,String currency) { transactionId=id; senderId=sender; receiverId=receiver; this.amount=amount; this.currency=currency; status="PENDING"; createdAt=Instant.now(); }
    public String getTransactionId(){return transactionId;} public String getSenderId(){return senderId;} public String getReceiverId(){return receiverId;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}