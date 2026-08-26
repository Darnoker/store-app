package com.github.darnoker.gatewayservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class GatewayRoutingIntegrationTest {

    private static HttpServer upstream;
    private static final AtomicReference<String> lastRequestPath = new AtomicReference<>();
    private static final AtomicReference<String> lastRequestQuery = new AtomicReference<>();

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void startUpstream() throws IOException {
        upstream = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        upstream.createContext("/", GatewayRoutingIntegrationTest::respond);
        upstream.start();
    }

    @AfterAll
    static void stopUpstream() {
        upstream.stop(0);
    }

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) {
        registry.add("gateway.product-service-url", () -> "http://localhost:" + upstream.getAddress().getPort());
    }

    @Test
    void forwardsPublicProductRequestsWithoutChangingPathOrQuery() throws Exception {
        mvc.perform(get("/products").queryParam("type", "BOOK"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"source\":\"product-service\"}"));

        assertThat(lastRequestPath.get()).isEqualTo("/products");
        assertThat(lastRequestQuery.get()).isEqualTo("type=BOOK");
    }

    @Test
    void permitsAdminToCreateProductAndForwardsRequestBody() throws Exception {
        mvc.perform(post("/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"name\":\"Book\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsUnauthenticatedOrderRequests() throws Exception {
        mvc.perform(get("/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnmappedRequests() throws Exception {
        mvc.perform(get("/internal"))
                .andExpect(status().isUnauthorized());
    }

    private static void respond(HttpExchange exchange) throws IOException {
        lastRequestPath.set(exchange.getRequestURI().getPath());
        lastRequestQuery.set(exchange.getRequestURI().getRawQuery());
        byte[] response = "{\"source\":\"product-service\"}".getBytes(StandardCharsets.UTF_8);
        int status = "POST".equals(exchange.getRequestMethod()) ? HttpStatus.CREATED.value() : HttpStatus.OK.value();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
