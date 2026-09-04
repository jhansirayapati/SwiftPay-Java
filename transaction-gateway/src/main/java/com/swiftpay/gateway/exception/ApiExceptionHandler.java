package com.swiftpay.gateway.exception;

import java.time.Instant; import java.util.Map; import java.util.UUID;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;

@RestControllerAdvice public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e){return body(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",e.getBindingResult().getFieldError().getDefaultMessage());}
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> conflict(IllegalArgumentException e){return body(HttpStatus.CONFLICT,"IDEMPOTENCY_CONFLICT",e.getMessage());}
    private ResponseEntity<?> body(HttpStatus status,String code,String message){return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"code",code,"message",message,"correlationId",UUID.randomUUID().toString()));}
}