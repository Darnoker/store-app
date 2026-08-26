package com.github.darnoker.userservice.identity;

import com.github.darnoker.userservice.user.UserService;
import com.github.darnoker.userservice.user.model.User;
import com.github.darnoker.userservice.user.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
class LocalIdentityAdapter implements IdentityRegistration, CredentialAuthentication {

    private final CredentialRepository credentials;

    private final UserRepository users;

    private final PasswordEncoder encoder;

    public void register(UUID userId, String password) {
        credentials.save(userId, encoder.encode(password));
    }

    public Optional<UUID> authenticate(String email, String password) {
        return users.findByEmail(UserService.normalize(email))
                .filter(u -> credentials.findPasswordHashByUserId(u.id()).map(hash -> encoder.matches(password, hash)).orElse(false))
                .map(User::id);
    }
}
