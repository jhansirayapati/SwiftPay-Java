package com.swiftpay.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class GatewayConfig {
    @Bean NewTopic initiatedTopic(){return TopicBuilder.name("swiftpay.payment.initiated").partitions(3).replicas(1).build();}
    @Bean org.springframework.kafka.support.serializer.JsonSerializer<Object> jsonSerializer(){return new org.springframework.kafka.support.serializer.JsonSerializer<>();}
}