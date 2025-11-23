package com.phitrading.exchange.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderRowView {
    String symbol;
    String side;            // BUY or SELL
    String status;          // EXECUTED, PENDING, CANCELED
    BigDecimal quantity;    // display-friendly quantity
    BigDecimal executionPrice;
    LocalDateTime createdAt;
    LocalDateTime executedAt;

    BigDecimal realizedPnl;   // nullable; shown for SELL if available
    String realizedPnlSign;   // positive | negative | zero | none
}
