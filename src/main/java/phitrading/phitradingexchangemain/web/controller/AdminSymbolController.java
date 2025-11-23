package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.AdminSymbolService;
import com.phitrading.exchange.integration.dto.CreateInstrumentRequest;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

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
        model.addAttribute("pageTitle", "Admin – Symbols");
        log.info("Loading Admin Symbols page");
        if (!model.containsAttribute("createForm")) {
            model.addAttribute("createForm", new CreateInstrumentRequest());
        }
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
    public String createSymbol(@Valid @ModelAttribute("createForm") CreateInstrumentRequest createForm,
                               BindingResult bindingResult,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            // Reload instruments and return same view to display field errors
            List<InstrumentPriceDto> instruments;
            try {
                instruments = adminSymbolService.getAllInstruments();
            } catch (Exception ex) {
                log.error("Error fetching instruments while handling validation errors", ex);
                instruments = Collections.emptyList();
                model.addAttribute("error", "Failed to fetch instruments: " + ex.getMessage());
            }
            model.addAttribute("pageTitle", "Admin – Symbols");
            model.addAttribute("instruments", instruments);
            model.addAttribute("symbolCount", instruments.size());
            return "admin-symbols";
        }

        try {
            // Normalize symbol to uppercase
            String symbol = createForm.getSymbol() != null ? createForm.getSymbol().trim().toUpperCase() : null;
            InstrumentPriceDto created = adminSymbolService.addSymbol(symbol, createForm.getName(), createForm.getInitialPrice());
            log.info("Successfully created/updated instrument: {} (lastPrice={})", created.getSymbol(), created.getLastPrice());
            redirectAttributes.addFlashAttribute("message", "Symbol " + created.getSymbol() + " created successfully.");
        } catch (Exception ex) {
            log.error("Error creating symbol via pricing service", ex);
            redirectAttributes.addFlashAttribute("error", "Failed to create symbol: " + ex.getMessage());
        }
        // PRG pattern
        return "redirect:/admin/symbols";
    }
}
