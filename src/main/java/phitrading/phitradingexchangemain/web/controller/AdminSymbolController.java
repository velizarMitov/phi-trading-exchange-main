package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.AdminSymbolService;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminSymbolController {

    private static final Logger log = LoggerFactory.getLogger(AdminSymbolController.class);
    private final AdminSymbolService adminSymbolService;

    public AdminSymbolController(AdminSymbolService adminSymbolService) {
        this.adminSymbolService = adminSymbolService;
    }

    @GetMapping("/symbols")
    public String symbols(Model model) {
        model.addAttribute("pageTitle", "Admin - Symbols");
        List<InstrumentPriceDto> instruments;
        try {
            instruments = adminSymbolService.getAllInstruments();
        } catch (Exception ex) {
            log.error("Error fetching instruments from pricing service", ex);
            instruments = Collections.emptyList();
            model.addAttribute("error", "Failed to fetch instruments: " + ex.getMessage());
        }
        model.addAttribute("instruments", instruments);
        model.addAttribute("symbolCount", instruments.size());
        return "admin-symbols";
    }

    @PostMapping("/symbols")
    public String createSymbol(@RequestParam String symbol,
                              @RequestParam String name,
                              @RequestParam("price") BigDecimal initialPrice,
                              Model model) {
        model.addAttribute("pageTitle", "Admin - Symbols");
        try {
            InstrumentPriceDto created = adminSymbolService.addSymbol(symbol, name, initialPrice);
            // Use PRG pattern so that the list reloads from pricing service
            return "redirect:/admin/symbols";
        } catch (Exception ex) {
            log.error("Error creating symbol via pricing service", ex);
            model.addAttribute("error", "Failed to create symbol: " + ex.getMessage());
            // On error, still show the page and load current instruments
            List<InstrumentPriceDto> instruments;
            try {
                instruments = adminSymbolService.getAllInstruments();
            } catch (Exception fetchEx) {
                log.error("Error fetching instruments after create failure", fetchEx);
                instruments = Collections.emptyList();
            }
            model.addAttribute("instruments", instruments);
            model.addAttribute("symbolCount", instruments.size());
            return "admin-symbols";
        }
    }
}
