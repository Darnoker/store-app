package com.github.darnoker.userservice.user.persistence;

import com.github.darnoker.userservice.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return map(jpaRepository.save(map(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::map);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::map);
    }

    private UserEntity map(User u) {
        return new UserEntity(u.id(), u.email(), u.firstName(), u.lastName(), u.role(), u.status(), u.createdAt(), u.updatedAt());
    }

    private User map(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getFirstName(), e.getLastName(), e.getRole(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
