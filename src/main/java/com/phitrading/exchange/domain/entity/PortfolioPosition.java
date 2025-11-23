package com.phitrading.exchange.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_positions",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_symbol", columnNames = {"user_id", "symbol"}))
@Getter
@Setter
@NoArgsConstructor
public class PortfolioPosition {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
