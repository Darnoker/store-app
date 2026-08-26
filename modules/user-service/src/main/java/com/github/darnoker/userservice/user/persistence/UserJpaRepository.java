package com.github.darnoker.userservice.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
