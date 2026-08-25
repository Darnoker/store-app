package com.github.darnoker.common.identity;

import java.util.Optional;

/**
 * Provides the application user authenticated for the current request.
 */
public interface CurrentUserProvider {
    Optional<AuthenticatedUser> currentUser();
}
