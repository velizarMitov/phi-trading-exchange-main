package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trade")
public class TradeController {

    @GetMapping("/buy")
    public String buy(Model model) {
        model.addAttribute("pageTitle", "Buy");
        model.addAttribute("currentPrice", null);
        model.addAttribute("position", null);
        return "trade-buy";
    }

    @PostMapping("/buy")
    public String buySubmit(@RequestParam String symbol, @RequestParam Integer quantity, Model model) {
        // TODO: Implement buy order logic
        // 1. Fetch current price from pricing service
        // 2. Validate sufficient cash balance
        // 3. Create order record
        // 4. Update portfolio position
        model.addAttribute("pageTitle", "Buy");
        model.addAttribute("message", "Order placed for " + quantity + " shares of " + symbol);
        return "redirect:/orders";
    }

    @GetMapping("/sell")
    public String sell(Model model) {
        model.addAttribute("pageTitle", "Sell");
        model.addAttribute("currentPrice", null);
        model.addAttribute("position", null);
        return "trade-sell";
    }

    @PostMapping("/sell")
    public String sellSubmit(@RequestParam String symbol, @RequestParam Integer quantity, Model model) {
        // TODO: Implement sell order logic
        // 1. Fetch current price from pricing service
        // 2. Validate sufficient shares in position
        // 3. Create order record
        // 4. Update portfolio position
        model.addAttribute("pageTitle", "Sell");
        model.addAttribute("message", "Order placed to sell " + quantity + " shares of " + symbol);
        return "redirect:/orders";
    }
}
