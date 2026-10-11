package com.vaibhavwable.notifications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByOrderId(Long orderId);

    List<Notification> findTop100ByOrderByIdDesc();

    List<Notification> findByCustomerEmailOrderByIdDesc(String customerEmail);
}
