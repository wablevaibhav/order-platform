package com.vaibhavwable.orders;

import com.vaibhavwable.orders.OrderDtos.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OrderService service;

    @Test
    void createReturns201WithOrder() throws Exception {
        when(service.create(any())).thenReturn(
                new OrderResponse(1L, "buyer@example.com", "Keyboard", 2, Order.Status.CREATED, Instant.now()));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"buyer@example.com\",\"product\":\"Keyboard\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createRejectsInvalidPayload() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"not-an-email\",\"product\":\"\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerEmail").exists())
                .andExpect(jsonPath("$.errors.product").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void getReturns404ForUnknownOrder() throws Exception {
        when(service.get(99L)).thenThrow(new OrderNotFoundException(99L));

        mvc.perform(get("/api/orders/99")).andExpect(status().isNotFound());
    }
}
