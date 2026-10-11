package com.vaibhavwable.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService service;
    private final ObjectMapper objectMapper;

    public OrderEventListener(NotificationService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders.created", groupId = "notification-service")
    public void onMessage(String payload) {
        OrderCreatedEvent event;
        try {
            event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        } catch (JsonProcessingException e) {
            // A malformed message can never succeed on retry, so log it and move on instead of blocking the partition.
            log.warn("Discarding malformed order event: {}", e.getOriginalMessage());
            return;
        }
        if (event.orderId() == null || event.customerEmail() == null) {
            log.warn("Discarding order event with missing fields: {}", payload);
            return;
        }
        service.handle(event);
    }
}
