package com.phitrading.exchange.web.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * View DTO for instruments on the Market Overview page.
 */
public class MarketInstrumentView {
    private String symbol;
    private String name;
    private BigDecimal lastPrice;
    private BigDecimal previousClose;
    private BigDecimal changeAbs;
    private BigDecimal changePct;
    private boolean positive;
    private boolean negative;

    public MarketInstrumentView() {
    }

    public MarketInstrumentView(String symbol, String name, BigDecimal lastPrice, BigDecimal previousClose) {
        this.symbol = symbol;
        this.name = name;
        this.lastPrice = lastPrice;
        this.previousClose = previousClose;
        computeDerived();
    }

    public static MarketInstrumentView of(String symbol, String name, BigDecimal lastPrice, BigDecimal previousClose) {
        return new MarketInstrumentView(symbol, name, lastPrice, previousClose);
    }

    private void computeDerived() {
        if (lastPrice == null || previousClose == null) {
            this.changeAbs = BigDecimal.ZERO;
            this.changePct = BigDecimal.ZERO;
            this.positive = false;
            this.negative = false;
            return;
        }
        this.changeAbs = lastPrice.subtract(previousClose);
        if (previousClose.signum() != 0) {
            this.changePct = changeAbs
                .divide(previousClose, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.changePct = BigDecimal.ZERO;
        }
        this.positive = changeAbs.signum() > 0;
        this.negative = changeAbs.signum() < 0;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; computeDerived(); }
    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; computeDerived(); }
    public BigDecimal getChangeAbs() { return changeAbs; }
    public BigDecimal getChangePct() { return changePct; }
    public boolean isPositive() { return positive; }
    public boolean isNegative() { return negative; }
}
