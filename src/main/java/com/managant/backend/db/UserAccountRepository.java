package com.managant.backend.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
  Optional<UserAccountEntity> findByUsername(String username);
  Optional<UserAccountEntity> findByPersonId(Long personId);
  boolean existsByUsernameAndPersonIdNot(String username, Long personId);
}
