package com.github.darnoker.orderservice.security;

import com.github.darnoker.common.identity.AuthenticatedUser;
import com.github.darnoker.common.identity.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class SpringSecurityCurrentUser implements CurrentUserProvider {
    @Override
    public Optional<AuthenticatedUser> currentUser() {
        return authentication()
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .flatMap(this::toAuthenticatedUser);
    }

    private Optional<Authentication> authentication() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated);
    }

    private Optional<AuthenticatedUser> toAuthenticatedUser(Jwt jwt) {
        try {
            String userId = jwt.getClaimAsString("user_id");
            if (userId == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(UUID.fromString(userId), Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                    .orElse(List.of())
                    .stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableSet())));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
