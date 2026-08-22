package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import com.github.darnoker.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    @AfterEach
    void clearOrders() {
        orderRepository.deleteAll();
    }

    @Test
    void createsAndReturnsOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest()
                .customerId(10L)
                .productId(20L)
                .quantity(2)
                .price(new BigDecimal("19.99"));

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/orders/")))
                .andReturn();

        OrderDTO createdOrder = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(request.getCustomerId(), createdOrder.getCustomerId());
        assertEquals(request.getProductId(), createdOrder.getProductId());
        assertEquals(request.getQuantity(), createdOrder.getQuantity());
        assertEquals(request.getPrice(), createdOrder.getPrice());
        assertEquals("CREATED", createdOrder.getStatus());
        assertNotNull(createdOrder.getOrderId());
        assertNotNull(createdOrder.getCreatedAt());

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        MvcResult getResult = mockMvc.perform(get(URI.create(location)))
                .andExpect(status().isOk())
                .andReturn();

        OrderDTO foundOrder = objectMapper.readValue(getResult.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(createdOrder, foundOrder);
    }
}
