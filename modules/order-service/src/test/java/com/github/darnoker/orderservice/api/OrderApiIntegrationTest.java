package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.catalog.ProductCatalog;
import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.CreateOrderItemRequest;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.time.Instant;

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

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createsAndReturnsOrder() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest()
                .items(List.of(new CreateOrderItemRequest().productId(productId).quantity(2)));
        given(jwtDecoder.decode("valid-token")).willReturn(jwtFor(customerId));
        given(productCatalog.getProducts(List.of(productId)))
                .willReturn(List.of(new ProductCatalog.ProductSnapshot(productId, "The Witcher", "BOOK", new BigDecimal("19.99"))));

        MvcResult result = mvc.perform(post("/orders")
                        .contentType("application/json")
                        .header("Authorization", "Bearer valid-token")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/orders/")))
                .andReturn();

        OrderDTO createdOrder = jsonMapper.readValue(result.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(customerId, createdOrder.getCustomerId());
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
        MvcResult getResult = mvc.perform(get(URI.create(location)).header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andReturn();

        OrderDTO foundOrder = jsonMapper.readValue(getResult.getResponse().getContentAsString(), OrderDTO.class);
        assertEquals(createdOrder, foundOrder);
    }

    @Test
    void requiresATokenAndDoesNotExposeAnotherUsersOrder() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest().items(List.of(new CreateOrderItemRequest().productId(productId).quantity(1)));
        given(productCatalog.getProducts(List.of(productId)))
                .willReturn(List.of(new ProductCatalog.ProductSnapshot(productId, "The Witcher", "BOOK", new BigDecimal("19.99"))));
        given(jwtDecoder.decode("owner-token")).willReturn(jwtFor(ownerId));
        given(jwtDecoder.decode("other-token")).willReturn(jwtFor(otherUserId));

        mvc.perform(post("/orders").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        MvcResult created = mvc.perform(post("/orders").header("Authorization", "Bearer owner-token").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn();
        String location = created.getResponse().getHeader("Location");
        mvc.perform(get(URI.create(location)).header("Authorization", "Bearer other-token")).andExpect(status().isNotFound());
        MvcResult myOrders = mvc.perform(get("/orders/me").header("Authorization", "Bearer owner-token"))
                .andExpect(status().isOk()).andReturn();
        List<OrderDTO> orders = jsonMapper.readValue(myOrders.getResponse().getContentAsString(),
                jsonMapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class));
        assertEquals(1, orders.size());
        assertEquals(ownerId, orders.getFirst().getCustomerId());
    }

    private Jwt jwtFor(UUID userId) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token").header("alg", "RS256").claim("user_id", userId.toString()).claim("roles", List.of("USER"))
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
    }
}
