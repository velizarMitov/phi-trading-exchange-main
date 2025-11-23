package com.phitrading.exchange.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class RecentOrderView {
    String symbol;
    String side; // BUY or SELL
    BigDecimal quantity;
    BigDecimal executionPrice;
    LocalDateTime executedAt;
}
