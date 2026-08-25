package com.github.darnoker.orderservice.api;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderJwtSecurityIntegrationTest extends BaseApiTest {
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
    void acceptsATokenSignedByTheConfiguredJwks() throws Exception {
        mvc.perform(get("/orders/me").header("Authorization", "Bearer " + token(SIGNING_KEY, ISSUER, List.of(AUDIENCE), Instant.now().plusSeconds(60))))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsTokensWithAnInvalidSignatureIssuerAudienceOrExpiry() throws Exception {
        assertUnauthorized(token(rsaKey("other-key"), ISSUER, List.of(AUDIENCE), Instant.now().plusSeconds(60)));
        assertUnauthorized(token(SIGNING_KEY, "https://other-identity.test", List.of(AUDIENCE), Instant.now().plusSeconds(60)));
        assertUnauthorized(token(SIGNING_KEY, ISSUER, List.of("another-api"), Instant.now().plusSeconds(60)));
    }

    private void assertUnauthorized(String token) throws Exception {
        mvc.perform(get("/orders/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private static String token(RSAKey key, String issuer, List<String> audience, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(UUID.randomUUID().toString())
                .audience(audience)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .claim("user_id", UUID.randomUUID().toString())
                .claim("roles", List.of("USER"))
                .build();
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
