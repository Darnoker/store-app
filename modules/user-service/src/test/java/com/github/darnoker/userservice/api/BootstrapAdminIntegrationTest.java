package com.github.darnoker.userservice.api;

import com.github.darnoker.userservice.generated.model.AccessToken;
import com.github.darnoker.userservice.generated.model.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class BootstrapAdminIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper jsonMapper;

    @Test
    void bootstrapsAnAdminAccount() throws Exception {
        AccessToken token = jsonMapper.readValue(mvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(new LoginRequest()
                                .email("admin@storeapp.local")
                                .password("changeit-admin"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), AccessToken.class);

        assertNotNull(token.getAccessToken());
    }
}
