package com.github.darnoker.productservice.api;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sun.net.httpserver.HttpServer;
import com.github.darnoker.productservice.generated.model.CreateProductRequest;
import com.github.darnoker.productservice.generated.model.ProductType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductJwtSecurityIntegrationTest extends BaseApiTest {
    private static final String ISSUER = "https://identity.test";
    private static final String AUDIENCE = "storeapp-api";
    private static final RSAKey SIGNING_KEY = rsaKey("test-key");
    private static final HttpServer JWKS_SERVER = jwksServer(SIGNING_KEY);

    @DynamicPropertySource
    static void identityProperties(DynamicPropertyRegistry registry) {
        registry.add("user-service.jwk-set-uri", () -> "http://localhost:" + JWKS_SERVER.getAddress().getPort() + "/oauth2/jwks");
        registry.add("user-service.issuer", () -> ISSUER);
        registry.add("user-service.audience", () -> AUDIENCE);
    }

    @AfterAll
    static void stopJwksServer() {
        JWKS_SERVER.stop(0);
    }

    @Test
    void allowsPublicCatalogueReadsWithoutAToken() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    void requiresAnAdminTokenToCreateAProduct() throws Exception {
        CreateProductRequest request = productRequest();
        mvc.perform(post("/products").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/products").header("Authorization", "Bearer " + token("USER"))
                        .contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/products").header("Authorization", "Bearer " + token("ADMIN"))
                        .contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsTokensWithAnInvalidSignatureIssuerAudienceOrExpiry() throws Exception {
        assertUnauthorized(token(rsaKey("other-key"), ISSUER, List.of(AUDIENCE), Instant.now().plusSeconds(60), "ADMIN"));
        assertUnauthorized(token(SIGNING_KEY, "https://other-identity.test", List.of(AUDIENCE), Instant.now().plusSeconds(60), "ADMIN"));
        assertUnauthorized(token(SIGNING_KEY, ISSUER, List.of("another-api"), Instant.now().plusSeconds(60), "ADMIN"));
        assertUnauthorized(token(SIGNING_KEY, ISSUER, List.of(AUDIENCE), Instant.now().minusSeconds(1), "ADMIN"));
    }

    private void assertUnauthorized(String jwt) throws Exception {
        mvc.perform(post("/products").header("Authorization", "Bearer " + jwt)
                        .contentType("application/json").content(jsonMapper.writeValueAsString(productRequest())))
                .andExpect(status().isUnauthorized());
    }

    private static CreateProductRequest productRequest() {
        return new CreateProductRequest().name("The Witcher").description("Fantasy book").price(new BigDecimal("49.99"))
                .productType(ProductType.BOOK)
                .details(Map.of("isbn", "978-83-0000-000-0", "pages", 320, "author", "Andrzej Sapkowski", "publisher", "Example Publisher", "language", "PL"));
    }

    private static String token(String... roles) {
        return token(SIGNING_KEY, ISSUER, List.of(AUDIENCE), Instant.now().plusSeconds(60), roles);
    }

    private static String token(RSAKey key, String issuer, List<String> audience, Instant expiresAt, String... roles) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer).subject(UUID.randomUUID().toString()).audience(audience)
                .issuedAt(Instant.now()).expiresAt(expiresAt)
                .claim("user_id", UUID.randomUUID().toString()).claim("roles", List.of(roles)).build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.getKeyID()).build(), claims))
                .getTokenValue();
    }

    private static RSAKey rsaKey(String keyId) {
        try {
            return new RSAKeyGenerator(2048).keyID(keyId).generate();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate a test RSA key", exception);
        }
    }

    private static HttpServer jwksServer(RSAKey key) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            byte[] response = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
            server.createContext("/oauth2/jwks", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to start the test JWKS server", exception);
        }
    }
}
