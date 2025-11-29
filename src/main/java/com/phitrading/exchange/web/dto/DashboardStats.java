package com.phitrading.exchange.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private BigDecimal cashBalance;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalAccountValue;
    private BigDecimal realizedPl; // not tracked yet -> set to 0
    private long positionsCount;
    private long executedOrdersCount;
    private LocalDateTime lastUpdated;
}
