package com.vaibhavwable.orders;

import java.time.Instant;

/** Event published to the {@code orders.created} topic. The notification-service has a matching copy. */
public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        String product,
        int quantity,
        Instant createdAt) {

    public static OrderCreatedEvent from(Order o) {
        return new OrderCreatedEvent(o.getId(), o.getCustomerEmail(), o.getProduct(),
                o.getQuantity(), o.getCreatedAt());
    }
}
