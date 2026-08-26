package com.github.darnoker.userservice.api;

import com.github.darnoker.userservice.generated.model.AccessToken;
import com.github.darnoker.userservice.generated.model.LoginRequest;
import com.github.darnoker.userservice.generated.model.RegisterRequest;
import com.github.darnoker.userservice.generated.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class UserApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper jsonMapper;

    @Test
    void registersLogsInAndReturnsTheAuthenticatedProfile() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        RegisterRequest registration = new RegisterRequest().email(email.toUpperCase()).password("a-secure-password").firstName("Ada").lastName("Lovelace");

        String registered = mvc.perform(post("/auth/register").contentType("application/json").content(jsonMapper.writeValueAsString(registration)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UserProfile profile = jsonMapper.readValue(registered, UserProfile.class);
        assertEquals(email, profile.getEmail());
        assertEquals("USER", profile.getRoles().getFirst());

        mvc.perform(post("/auth/register").contentType("application/json").content(jsonMapper.writeValueAsString(registration)))
                .andExpect(status().isConflict());
        mvc.perform(post("/auth/login").contentType("application/json").content(jsonMapper.writeValueAsString(new LoginRequest().email(email).password("wrong-password"))))
                .andExpect(status().isUnauthorized());

        AccessToken token = jsonMapper.readValue(mvc.perform(post("/auth/login").contentType("application/json").content(jsonMapper.writeValueAsString(new LoginRequest().email(email).password("a-secure-password"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), AccessToken.class);
        assertEquals("Bearer", token.getTokenType());
        assertNotNull(token.getAccessToken());

        UserProfile current = jsonMapper.readValue(mvc.perform(get("/users/me").header("Authorization", "Bearer " + token.getAccessToken()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserProfile.class);
        assertEquals(profile.getUserId(), current.getUserId());
        mvc.perform(get("/oauth2/jwks")).andExpect(status().isOk());
    }

    @Test
    void rejectsLoginForAnUnknownUser() throws Exception {
        LoginRequest request = new LoginRequest()
                .email("missing-" + UUID.randomUUID() + "@example.com")
                .password("a-secure-password");

        mvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresAValidBearerTokenToReadTheCurrentProfile() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/users/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidRegistrationRequests() throws Exception {
        RegisterRequest invalidEmail = new RegisterRequest()
                .email("not-an-email")
                .password("a-secure-password");
        RegisterRequest shortPassword = new RegisterRequest()
                .email("user-" + UUID.randomUUID() + "@example.com")
                .password("too-short");

        mvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(shortPassword)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exposesThePublicRsaSigningKey() throws Exception {
        String jwks = mvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("RSA", jsonMapper.readTree(jwks).get("keys").get(0).get("kty").asString());
        assertEquals("user-service-rs256", jsonMapper.readTree(jwks).get("keys").get(0).get("kid").asString());
    }
}
