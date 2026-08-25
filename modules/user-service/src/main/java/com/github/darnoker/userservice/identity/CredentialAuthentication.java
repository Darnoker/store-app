package com.github.darnoker.userservice.identity;

import java.util.Optional;
import java.util.UUID;

public interface CredentialAuthentication {
    Optional<UUID> authenticate(String email, String password);
}
