package com.github.darnoker.userservice.user.persistence;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository {

    void save(UUID userId, String passwordHash);

    Optional<String> findPasswordHashByUserId(UUID userId);
}
