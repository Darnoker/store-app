package com.github.darnoker.userservice.api;

import com.github.darnoker.common.identity.CurrentUserProvider;
import com.github.darnoker.userservice.generated.api.AuthenticationApi;
import com.github.darnoker.userservice.generated.api.UsersApi;
import com.github.darnoker.userservice.generated.model.*;
import com.github.darnoker.userservice.identity.*;
import com.github.darnoker.userservice.user.*;
import com.github.darnoker.userservice.user.model.User;
import com.nimbusds.jose.jwk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class UserController implements AuthenticationApi, UsersApi {
    private final AuthenticationService auth;
    private final UserService userService;
    private final RSAKey key;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    public ResponseEntity<UserProfile> register(RegisterRequest request) {
        User u = auth.register(request.getEmail(), request.getPassword(), request.getFirstName(), request.getLastName());
        return ResponseEntity.status(HttpStatus.CREATED).body(profile(u));
    }

    public ResponseEntity<AccessToken> login(LoginRequest request) {
        return ResponseEntity.ok(new AccessToken().accessToken(auth.login(request.getEmail(), request.getPassword())).tokenType("Bearer").expiresIn(3600L));
    }

    public ResponseEntity<UserProfile> getCurrentUser() {
        return currentUserProvider.currentUser()
                .flatMap(user -> userService.findById(user.userId()))
                .map(this::profile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> getJwks() {
        return ResponseEntity.ok(Map.of("keys", List.of(key.toPublicJWK().toJSONObject())));
    }

    private UserProfile profile(User u) {
        return new UserProfile()
                .userId(u.id())
                .email(u.email())
                .firstName(u.firstName())
                .lastName(u.lastName())
                .roles(List.of(u.role().name()))
                .status(u.status().name())
                .createdAt(OffsetDateTime.ofInstant(u.createdAt(), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(u.updatedAt(), ZoneOffset.UTC));
    }
}
