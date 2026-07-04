package com.managant.backend.db;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionTokenEntity, Long> {
  Optional<SessionTokenEntity> findByTokenAndActiveTrue(String token);

  default Optional<SessionTokenEntity> findValidActiveByToken(String token) {
    return findByTokenAndActiveTrue(token)
        .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isAfter(LocalDateTime.now()));
  }
}
