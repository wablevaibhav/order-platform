package com.vaibhavwable.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates the notification for an order. Idempotent: Kafka delivers at least once,
     * so an event for an order that already has a notification is skipped.
     *
     * @return true if a notification was created, false if it was a duplicate
     */
    @Transactional
    public boolean handle(OrderCreatedEvent event) {
        if (repository.existsByOrderId(event.orderId())) {
            log.info("Skipping duplicate event for order {}", event.orderId());
            return false;
        }
        String message = "Order #%d confirmed: %d x %s".formatted(event.orderId(), event.quantity(), event.product());
        repository.save(new Notification(event.orderId(), event.customerEmail(), message));
        log.info("Created notification for order {}", event.orderId());
        return true;
    }

    @Transactional(readOnly = true)
    public List<Notification> find(String email) {
        return (email == null || email.isBlank())
                ? repository.findTop100ByOrderByIdDesc()
                : repository.findByCustomerEmailOrderByIdDesc(email.trim().toLowerCase());
    }
}
