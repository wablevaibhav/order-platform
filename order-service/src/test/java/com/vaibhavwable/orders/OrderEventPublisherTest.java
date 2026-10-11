package com.vaibhavwable.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void publishesJsonPayloadKeyedByOrderId() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        OrderEventPublisher publisher = new OrderEventPublisher(kafkaTemplate, mapper);

        publisher.publish(new OrderCreatedEvent(42L, "buyer@example.com", "Keyboard", 2, Instant.parse("2026-10-11T10:00:00Z")));

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("orders.created"), eq("42"), payload.capture());

        JsonNode json = mapper.readTree(payload.getValue());
        assertThat(json.get("orderId").asLong()).isEqualTo(42L);
        assertThat(json.get("customerEmail").asText()).isEqualTo("buyer@example.com");
        assertThat(json.get("product").asText()).isEqualTo("Keyboard");
        assertThat(json.get("quantity").asInt()).isEqualTo(2);
    }
}
