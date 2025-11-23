package com.phitrading.exchange.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal executionPrice;

    // Realized profit/loss for SELL orders; null for BUY or when not applicable
    @Column(precision = 19, scale = 4)
    private BigDecimal realizedPnl;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime executedAt;

    public enum OrderSide { BUY, SELL }
    public enum OrderStatus { PENDING, EXECUTED, CANCELED }
}
