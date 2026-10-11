package com.vaibhavwable.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    NotificationService service;

    private OrderEventListener listener() {
        return new OrderEventListener(service, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void validMessageIsHandedToService() {
        String payload = """
                {"orderId":42,"customerEmail":"buyer@example.com","product":"Keyboard","quantity":2,
                 "createdAt":"2026-10-11T10:00:00Z","someFutureField":"ignored"}
                """;

        listener().onMessage(payload);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(service).handle(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(42L);
        assertThat(captor.getValue().product()).isEqualTo("Keyboard");
    }

    @Test
    void malformedMessageIsDiscardedWithoutThrowing() {
        assertThatCode(() -> listener().onMessage("{not json")).doesNotThrowAnyException();
        verify(service, never()).handle(any());
    }

    @Test
    void messageWithMissingFieldsIsDiscarded() {
        listener().onMessage("{\"product\":\"Keyboard\",\"quantity\":1}");
        verify(service, never()).handle(any());
    }
}
