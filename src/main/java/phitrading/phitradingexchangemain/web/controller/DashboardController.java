package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("username", "Trader");
        model.addAttribute("cashBalance", new BigDecimal("10000.00"));
        model.addAttribute("portfolioValue", new BigDecimal("0.00"));
        model.addAttribute("accountValue", new BigDecimal("10000.00"));
        model.addAttribute("openOrdersCount", 0);
        model.addAttribute("positionsCount", 0);
        model.addAttribute("recentTrades", List.of(
                Map.of("symbol", "AAPL", "side", "BUY", "qty", 5, "price", "180.25"),
                Map.of("symbol", "TSLA", "side", "SELL", "qty", 2, "price", "240.10"),
                Map.of("symbol", "NVDA", "side", "BUY", "qty", 1, "price", "485.00")
        ));
        return "dashboard";
    }
}
