package com.phitrading.exchange.domain.repository;

import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, UUID> {
    Optional<PortfolioPosition> findByUserAndSymbol(UserAccount user, String symbol);

    // Fetch all positions for a given username
    java.util.List<PortfolioPosition> findAllByUser_Username(String username);

    // Count positions for a given username
    long countByUser_Username(String username);

    // Check if any position exists for a symbol (used by admin delete guard)
    boolean existsBySymbol(String symbol);

    // Fetch all positions for a given symbol (used for safe delete)
    java.util.List<PortfolioPosition> findBySymbol(String symbol);

    // Count positions for a given symbol (helper)
    long countBySymbol(String symbol);
}
