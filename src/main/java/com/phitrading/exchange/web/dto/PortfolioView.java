package com.phitrading.exchange.web.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class PortfolioView {
    @Singular
    List<PortfolioRowView> rows;

    BigDecimal totalCost;
    BigDecimal totalCurrentValue;
    BigDecimal totalPnlAbs;
    BigDecimal totalPnlPct;
}
