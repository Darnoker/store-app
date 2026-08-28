package com.github.darnoker.userservice.identity;

import com.github.darnoker.userservice.user.*;
import com.github.darnoker.userservice.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService users;

    private final IdentityRegistration registration;

    private final CredentialAuthentication authentication;

    private final AccessTokenIssuer issuer;

    @Transactional
    public User register(String email, String password, String first, String last) {
        return register(email, password, first, last, UserRole.USER);
    }

    @Transactional
    public User registerAdmin(String email, String password) {
        return register(email, password, null, null, UserRole.ADMIN);
    }

    private User register(String email, String password, String first, String last, UserRole role) {
        User user = users.register(email, first, last, role);
        registration.register(user.id(), password);
        log.info("Registered identity for user {}", user.id());
        return user;
    }

    public String login(String email, String password) {
        User user = authentication.authenticate(email, password).flatMap(users::findById).filter(u -> u.status() == UserStatus.ACTIVE).orElseThrow(InvalidCredentialsException::new);
        String accessToken = issuer.issue(user);
        log.info("Authenticated user {}", user.id());
        return accessToken;
    }

    public static class InvalidCredentialsException extends RuntimeException {
    }
}
