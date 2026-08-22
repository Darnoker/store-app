package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiIntegrationTest extends BaseApiTest {

    @Test
    void createsAndReturnsOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest()
                .customerId(10L)
                .productId(20L)
                .quantity(2)
                .price(new BigDecimal("19.99"));

        MvcResult result = mvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/orders/")))
                .andReturn();

        OrderDTO createdOrder = jsonMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(request.getCustomerId(), createdOrder.getCustomerId());
        assertEquals(request.getProductId(), createdOrder.getProductId());
        assertEquals(request.getQuantity(), createdOrder.getQuantity());
        assertEquals(request.getPrice(), createdOrder.getPrice());
        assertEquals("CREATED", createdOrder.getStatus());
        assertNotNull(createdOrder.getOrderId());
        assertNotNull(createdOrder.getCreatedAt());

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        MvcResult getResult = mvc.perform(get(URI.create(location)))
                .andExpect(status().isOk())
                .andReturn();

        OrderDTO foundOrder = jsonMapper.readValue(getResult.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(createdOrder, foundOrder);
    }
}
