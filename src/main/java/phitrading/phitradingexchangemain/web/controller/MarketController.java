package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.MarketService;
import com.phitrading.exchange.web.dto.MarketInstrumentView;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalTime;
import java.util.List;

@Controller
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/market")
    public String market(Model model, Authentication auth) {
        String username = auth != null ? auth.getName() : "anonymous";
        List<MarketInstrumentView> instruments = marketService.getMarketOverviewForUser(username);

        model.addAttribute("marketInstruments", instruments);
        model.addAttribute("selectedCurrency", "USD");
        model.addAttribute("serverTime", LocalTime.now());
        model.addAttribute("pageTitle", "Market Overview");
        return "market-overview";
    }

    @GetMapping(value = "/api/market/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<MarketInstrumentView> marketOverviewApi(Authentication auth) {
        String username = auth != null ? auth.getName() : "anonymous";
        return marketService.getMarketOverviewForUser(username);
    }
}
