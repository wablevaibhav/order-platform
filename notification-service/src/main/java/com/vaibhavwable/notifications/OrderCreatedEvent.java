package com.vaibhavwable.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/** Mirror of the event published by order-service. Unknown fields are ignored so the producer can evolve. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        String product,
        int quantity,
        Instant createdAt) {
}
