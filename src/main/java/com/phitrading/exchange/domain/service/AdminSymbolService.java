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
}

