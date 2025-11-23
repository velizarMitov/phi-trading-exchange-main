package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.service.AdminSymbolService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.CreateInstrumentRequest;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminSymbolServiceImpl implements AdminSymbolService {

    private static final Logger log = LoggerFactory.getLogger(AdminSymbolServiceImpl.class);

    private final PricingServiceClient pricingServiceClient;

    public AdminSymbolServiceImpl(PricingServiceClient pricingServiceClient) {
        this.pricingServiceClient = pricingServiceClient;
    }

    @Override
    public InstrumentPriceDto addSymbol(String symbol, String name, BigDecimal initialPrice) {
        CreateInstrumentRequest request = new CreateInstrumentRequest(symbol, name, initialPrice);
        log.info("Creating instrument in pricing service: symbol={}, name={}, initialPrice={}", symbol, name, initialPrice);
        try {
            InstrumentPriceDto created = pricingServiceClient.createOrUpdateInstrument(request);
            log.info("Created instrument in pricing service: symbol={}, lastPrice={}", created.getSymbol(), created.getLastPrice());
            return created;
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            log.error("Failed to create instrument in pricing service. status={}, body={}", e.status(), responseBody);
            throw e;
        }
    }

    @Override
    public List<InstrumentPriceDto> getAllInstruments() {
        log.info("Fetching all instruments from pricing service");
        return pricingServiceClient.getAllInstruments();
    }
}

