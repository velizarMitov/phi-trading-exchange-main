package com.phitrading.exchange.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PortfolioRowView {
    String symbol;
    String name; // optional
    BigDecimal quantity;
    BigDecimal averagePrice;
    BigDecimal currentPrice;
    BigDecimal pnlAbs;
    BigDecimal pnlPct;
}
