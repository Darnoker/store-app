package com.github.darnoker.userservice.user.persistence;

import com.github.darnoker.userservice.user.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);
}
