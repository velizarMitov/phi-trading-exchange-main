package com.phitrading.exchange.domain.repository;

import com.phitrading.exchange.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    // WARNING: Using derived query methods that expect a single row may cause
    // JPA to call getSingleResult() under the hood. If duplicate rows exist (e.g. due to
    // legacy data), that would raise NonUniqueResultException on startup/auth.
    // Prefer the safe variants below that order by updatedAt and take the latest.
    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    // Safe variants: intentionally take the latest record if duplicates exist.
    // We order by updatedAt desc and return Optional first result to avoid getSingleResult().
    Optional<UserAccount> findFirstByUsernameOrderByUpdatedAtDesc(String username);

    Optional<UserAccount> findFirstByEmailOrderByUpdatedAtDesc(String email);
}
