package com.vaibhavwable.orders;

import com.vaibhavwable.orders.OrderDtos.CreateOrderRequest;
import com.vaibhavwable.orders.OrderDtos.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventPublisher publisher;

    public OrderService(OrderRepository repository, OrderEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order saved = repository.save(new Order(
                request.customerEmail().trim().toLowerCase(),
                request.product().trim(),
                request.quantity()));
        publisher.publish(OrderCreatedEvent.from(saved));
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return repository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> latest() {
        return repository.findTop100ByOrderByIdDesc().stream().map(OrderResponse::from).toList();
    }
}
