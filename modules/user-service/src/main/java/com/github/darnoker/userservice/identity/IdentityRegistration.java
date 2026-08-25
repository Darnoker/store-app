package com.github.darnoker.userservice.identity;

import java.util.UUID;

public interface IdentityRegistration {
    void register(UUID userId, String password);
}
