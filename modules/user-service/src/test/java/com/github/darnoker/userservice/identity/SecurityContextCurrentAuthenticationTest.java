package com.github.darnoker.userservice.identity;

import com.github.darnoker.common.identity.AuthenticatedUser;
import com.github.darnoker.common.identity.CurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringSecurityCurrentUserTest {
    private final CurrentUserProvider currentUser = new SpringSecurityCurrentUser();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapsTheAuthenticatedJwtToAnApplicationUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("user_id", userId.toString())
                .claim("roles", java.util.List.of("USER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(jwt, null, List.of()));

        AuthenticatedUser authenticatedUser = currentUser.currentUser().orElseThrow();
        assertEquals(userId, authenticatedUser.userId());
        assertEquals(java.util.Set.of("USER"), authenticatedUser.roles());
    }

    @Test
    void returnsAnEmptyOptionalForMissingOrMalformedAuthenticationValues() {
        assertTrue(currentUser.currentUser().isEmpty());

        Jwt invalidUserId = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("user_id", "not-a-uuid")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(invalidUserId, null, List.of()));

        assertTrue(currentUser.currentUser().isEmpty());
    }
}
