package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.integration.dto.InstrumentPriceDto;

import java.math.BigDecimal;
import java.util.List;

public interface AdminSymbolService {
    InstrumentPriceDto addSymbol(String symbol, String name, BigDecimal initialPrice);

    /**
     * Fetch all instruments from the pricing service.
     */
    List<InstrumentPriceDto> getAllInstruments();

    /**
     * Remove a tradable symbol (ADMIN only). This will check if the symbol is in use
     * by any portfolio positions or orders and prevent deletion if so.
     *
     * @param symbol symbol to remove
     */
    void removeSymbol(String symbol);

    /**
     * Safely delete a symbol. If there are portfolio positions for the symbol, it will not delete
     * and will provide details who holds the symbol and counts for positions and orders.
     *
     * @param symbol symbol to delete
     * @return deletion result with details
     */
    com.phitrading.exchange.web.dto.AdminSymbolDeleteResult deleteSymbolSafely(String symbol);
}

