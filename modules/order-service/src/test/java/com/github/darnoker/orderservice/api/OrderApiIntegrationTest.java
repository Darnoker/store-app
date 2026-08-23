package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.catalog.ProductCatalog;
import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.CreateOrderItemRequest;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

class OrderApiIntegrationTest extends BaseApiTest {

    @MockitoBean
    private ProductCatalog productCatalog;

    @Test
    void createsAndReturnsOrder() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest()
                .customerId(customerId)
                .items(List.of(new CreateOrderItemRequest().productId(productId).quantity(2)));
        given(productCatalog.getProducts(List.of(productId)))
                .willReturn(List.of(new ProductCatalog.ProductSnapshot(productId, "The Witcher", "BOOK", new BigDecimal("19.99"))));

        MvcResult result = mvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/orders/")))
                .andReturn();

        OrderDTO createdOrder = jsonMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(request.getCustomerId(), createdOrder.getCustomerId());
        assertEquals(1, createdOrder.getItems().size());
        assertNotNull(createdOrder.getItems().getFirst().getOrderItemId());
        assertEquals(productId, createdOrder.getItems().getFirst().getProductId());
        assertEquals(new BigDecimal("19.99"), createdOrder.getItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("39.98"), createdOrder.getTotalAmount());
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
