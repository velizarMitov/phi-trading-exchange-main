package com.phitrading.exchange.web.dto;

import com.phitrading.exchange.common.CurrencyCode;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class DashboardView {
    BigDecimal cashBalance;
    BigDecimal totalCost;
    BigDecimal totalCurrentValue;
    BigDecimal totalAccountValue;
    BigDecimal totalPnlAbs;
    BigDecimal totalPnlPct;

    @Singular
    List<PortfolioRowView> topPositions;

    @Singular
    List<RecentOrderView> recentOrders;

    // Currency presentation layer
    CurrencyCode activeCurrency;
    List<CurrencyCode> supportedCurrencies;
}
