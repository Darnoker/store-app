package com.github.darnoker.userservice.user;

import com.github.darnoker.userservice.user.model.User;
import com.github.darnoker.userservice.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository users;
    private final Clock clock;

    @Transactional
    public User register(String email, String firstName, String lastName, UserRole role) {
        String normalized = normalize(email);
        if (users.findByEmail(normalized).isPresent()) {
            log.warn("Rejected registration for existing email {}", normalized);
            throw new DuplicateEmailException();
        }
        Instant now = Instant.now(clock);
        User user = users.save(new User(UUID.randomUUID(), normalized, firstName, lastName, role, UserStatus.ACTIVE, now, now));
        log.info("Registered user {} with role {}", user.id(), role);
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(normalize(email));
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return users.findById(id);
    }

    public static String normalize(String email) {
        return Objects.requireNonNull(email).trim().toLowerCase(Locale.ROOT);
    }

    public static class DuplicateEmailException extends RuntimeException {
    }
}
