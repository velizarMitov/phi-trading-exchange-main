package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;

@Controller
@RequestMapping("/admin")
public class AdminSymbolController {

    @GetMapping("/symbols")
    public String symbols(Model model) {
        model.addAttribute("pageTitle", "Admin - Symbols");
        model.addAttribute("symbols", Collections.emptyList());
        return "admin-symbols";
    }

    @PostMapping("/symbols")
    public String createSymbol(@RequestParam String symbol,
                              @RequestParam String name,
                              @RequestParam BigDecimal price,
                              Model model) {
        // TODO: Implement symbol creation logic
        // 1. Validate symbol doesn't already exist
        // 2. Validate price is positive
        // 3. Create new Symbol entity
        // 4. Save to database
        // 5. Initialize pricing service with this symbol

        model.addAttribute("pageTitle", "Admin - Symbols");
        model.addAttribute("symbols", Collections.emptyList());
        model.addAttribute("message", "Symbol " + symbol + " created successfully");
        return "admin-symbols";
    }
}
