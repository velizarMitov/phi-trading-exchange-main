package com.phitrading.exchange.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for safe delete operation in AdminSymbolService.
 */
public class AdminSymbolDeleteResult {
    private boolean deleted;
    private String symbol;
    private List<String> holderUsernames = new ArrayList<>();
    private long positionsCount;
    private long ordersCount;
    private String message;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<String> getHolderUsernames() {
        return holderUsernames;
    }

    public void setHolderUsernames(List<String> holderUsernames) {
        this.holderUsernames = holderUsernames;
    }

    public long getPositionsCount() {
        return positionsCount;
    }

    public void setPositionsCount(long positionsCount) {
        this.positionsCount = positionsCount;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
