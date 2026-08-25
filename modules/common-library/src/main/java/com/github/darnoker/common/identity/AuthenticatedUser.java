package com.github.darnoker.common.identity;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, Set<String> roles) {
    public AuthenticatedUser {
        Objects.requireNonNull(userId, "userId must not be null");
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
    }
}
