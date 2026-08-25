package com.github.darnoker.userservice.config;

import com.github.darnoker.userservice.identity.AuthenticationService;
import com.github.darnoker.userservice.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

@Configuration
@RequiredArgsConstructor
public class BootstrapAdmin {
    private final AuthenticationService auth;
    private final UserService users;

    @Bean
    ApplicationRunner bootstrap(@Value("${bootstrap-admin.email:}") String email, @Value("${bootstrap-admin.password:}") String password) {
        if (email.isBlank() != password.isBlank()) {
            throw new IllegalStateException("Both BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are required");
        }
        return _ -> {
            if (!email.isBlank() && users.findByEmail(email).isEmpty()) {
                auth.registerAdmin(email, password);
            }
        };
    }
}
