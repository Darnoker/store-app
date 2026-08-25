package com.github.darnoker.userservice.user.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class CredentialRepositoryAdapter implements CredentialRepository {

    private final CredentialJpaRepository jpaRepository;

    @Override
    public void save(UUID userId, String passwordHash) {
        jpaRepository.save(new CredentialEntity(userId, passwordHash));
    }

    @Override
    public Optional<String> findPasswordHashByUserId(UUID userId) {
        return jpaRepository.findById(userId).map(CredentialEntity::getPasswordHash);
    }
}
