package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.MarketInstrumentView;

import java.util.List;

public interface MarketService {
    List<MarketInstrumentView> getMarketOverviewForUser(String username);
}
