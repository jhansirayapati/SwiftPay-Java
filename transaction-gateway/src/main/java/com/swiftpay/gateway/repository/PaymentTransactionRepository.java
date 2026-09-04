package com.swiftpay.gateway.repository;

import com.swiftpay.gateway.entity.PaymentTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> { Optional<PaymentTransaction> findByTransactionId(String transactionId); }