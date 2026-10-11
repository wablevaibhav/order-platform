package com.vaibhavwable.orders;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotBlank @Email String customerEmail,
            @NotBlank String product,
            @Min(1) @Max(1000) int quantity) {
    }

    public record OrderResponse(
            Long id,
            String customerEmail,
            String product,
            int quantity,
            Order.Status status,
            Instant createdAt) {

        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getProduct(),
                    o.getQuantity(), o.getStatus(), o.getCreatedAt());
        }
    }
}
