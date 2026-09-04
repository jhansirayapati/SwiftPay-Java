package com.swiftpay.gateway.controller;

import com.swiftpay.gateway.dto.*;
import com.swiftpay.gateway.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/v1/payments")
public class PaymentController {
    private final PaymentService service; public PaymentController(PaymentService service){this.service=service;}
    @PostMapping public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request){return ResponseEntity.accepted().body(service.initiate(request));}
}