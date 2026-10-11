package com.vaibhavwable.orders;

import com.vaibhavwable.orders.OrderDtos.CreateOrderRequest;
import com.vaibhavwable.orders.OrderDtos.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository repository;

    @Mock
    OrderEventPublisher publisher;

    @Test
    void createSavesOrderAndPublishesEvent() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "id", 42L);
            return o;
        });
        OrderService service = new OrderService(repository, publisher);

        OrderResponse response = service.create(new CreateOrderRequest("  Buyer@Example.com ", " Keyboard ", 2));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.customerEmail()).isEqualTo("buyer@example.com");
        assertThat(response.product()).isEqualTo("Keyboard");

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(42L);
        assertThat(captor.getValue().customerEmail()).isEqualTo("buyer@example.com");
        assertThat(captor.getValue().quantity()).isEqualTo(2);
    }

    @Test
    void getThrowsWhenOrderMissing() {
        when(repository.findById(7L)).thenReturn(Optional.empty());
        OrderService service = new OrderService(repository, publisher);

        assertThatThrownBy(() -> service.get(7L)).isInstanceOf(OrderNotFoundException.class);
        verify(publisher, never()).publish(any());
    }
}
