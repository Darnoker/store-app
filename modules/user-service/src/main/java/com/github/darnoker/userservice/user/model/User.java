package com.github.darnoker.userservice.user.model;

import com.github.darnoker.userservice.user.*;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String email, String firstName, String lastName, UserRole role, UserStatus status,
                   Instant createdAt, Instant updatedAt) {
}
