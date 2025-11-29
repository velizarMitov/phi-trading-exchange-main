package com.phitrading.exchange.integration;

import com.phitrading.exchange.integration.dto.CreateInstrumentRequest;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.integration.dto.UpdatePriceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Feign client for communicating with the market-pricing-service
 */
@FeignClient(name = "marketPricingService", url = "${pricing.service.url}")
public interface PricingServiceClient {

    /**
     * Create or update an instrument in the pricing service
     * @param request the instrument details
     * @return the created/updated instrument
     */
    @PostMapping(value = "/api/instruments", consumes = "application/json")
    InstrumentPriceDto createOrUpdateInstrument(@RequestBody CreateInstrumentRequest request);

    /**
     * Update the price of an instrument
     * @param symbol the instrument symbol
     * @param request the new price
     * @return the updated instrument
     */
    @PutMapping(value = "/api/instruments/{symbol}/price", consumes = "application/json")
    InstrumentPriceDto updatePrice(@PathVariable("symbol") String symbol,
                                    @RequestBody UpdatePriceRequest request);

    /**
     * Get the current price for an instrument
     * @param symbol the instrument symbol
     * @return the current price information
     */
    @GetMapping("/api/instruments/{symbol}/price")
    InstrumentPriceDto getCurrentPrice(@PathVariable("symbol") String symbol);

    /**
     * Get all instruments with their current pricing information
     * @return list of instruments
     */
    @GetMapping("/api/instruments")
    List<InstrumentPriceDto> getAllInstruments();

    /**
     * Delete an instrument from the pricing service
     * @param symbol the instrument symbol
     */
    @DeleteMapping("/api/instruments/{symbol}")
    void deleteInstrument(@PathVariable("symbol") String symbol);
}
