package com.github.darnoker.userservice.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CredentialJpaRepository extends JpaRepository<CredentialEntity, UUID> {
}
