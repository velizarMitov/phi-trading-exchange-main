package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.integration.dto.InstrumentPriceDto;

import java.util.List;

public interface TradeService {
    List<InstrumentPriceDto> listInstruments();

    void buy(String username, String symbol, long quantity);

    /**
     * Execute a market SELL for the given user and symbol.
     * @param username authenticated username (non-null/non-blank)
     * @param symbol ticker symbol to sell
     * @param quantity quantity to sell (> 0)
     */
    void sell(String username, String symbol, long quantity);
}
