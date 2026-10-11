package com.vaibhavwable.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository repository;

    private final OrderCreatedEvent event =
            new OrderCreatedEvent(42L, "buyer@example.com", "Keyboard", 2, Instant.now());

    @Test
    void createsNotificationForNewOrder() {
        when(repository.existsByOrderId(42L)).thenReturn(false);
        NotificationService service = new NotificationService(repository);

        assertThat(service.handle(event)).isTrue();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(42L);
        assertThat(captor.getValue().getCustomerEmail()).isEqualTo("buyer@example.com");
        assertThat(captor.getValue().getMessage()).isEqualTo("Order #42 confirmed: 2 x Keyboard");
    }

    @Test
    void skipsDuplicateEvents() {
        when(repository.existsByOrderId(42L)).thenReturn(true);
        NotificationService service = new NotificationService(repository);

        assertThat(service.handle(event)).isFalse();
        verify(repository, never()).save(any());
    }
}
