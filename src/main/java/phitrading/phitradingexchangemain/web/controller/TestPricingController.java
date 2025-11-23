package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestPricingController {

    private final PricingServiceClient pricingServiceClient;

    public TestPricingController(PricingServiceClient pricingServiceClient) {
        this.pricingServiceClient = pricingServiceClient;
    }

    @GetMapping("/price/{symbol}")
    public InstrumentPriceDto getPrice(@PathVariable String symbol) {
        return pricingServiceClient.getCurrentPrice(symbol);
    }
}

