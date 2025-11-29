package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.service.MarketService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.MarketInstrumentView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketServiceImpl implements MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketServiceImpl.class);

    private final PricingServiceClient pricingClient;

    public MarketServiceImpl(PricingServiceClient pricingClient) {
        this.pricingClient = pricingClient;
    }

    @Override
    public List<MarketInstrumentView> getMarketOverviewForUser(String username) {
        log.info("Loading market overview for user={}", username);
        List<InstrumentPriceDto> all = pricingClient.getAllInstruments();
        return all.stream()
            .map(dto -> MarketInstrumentView.of(
                dto.getSymbol(),
                dto.getName(),
                dto.getLastPrice(),
                dto.getPreviousClose()
            ))
            .sorted(Comparator.comparing(MarketInstrumentView::getSymbol))
            .collect(Collectors.toList());
    }
}
