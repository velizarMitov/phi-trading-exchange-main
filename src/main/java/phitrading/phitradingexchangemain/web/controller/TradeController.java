package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.domain.service.TradeService;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.PortfolioRowView;
import com.phitrading.exchange.web.dto.BuyOrderForm;
import com.phitrading.exchange.web.dto.SellOrderForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/trade")
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final PortfolioService portfolioService;

    public TradeController(TradeService tradeService, PortfolioService portfolioService) {
        this.tradeService = tradeService;
        this.portfolioService = portfolioService;
    }

    @GetMapping("/buy")
    public String buy(@RequestParam(name = "symbol", required = false) String symbol,
                      Principal principal,
                      Model model) {
        model.addAttribute("pageTitle", "Buy");
        List<InstrumentPriceDto> instruments;
        try {
            instruments = tradeService.listInstruments();
        } catch (Exception ex) {
            log.error("Error fetching instruments for buy page", ex);
            instruments = Collections.emptyList();
            model.addAttribute("error", "Failed to load instruments: " + ex.getMessage());
        }

        // Prepare form (pre-select symbol if provided)
        BuyOrderForm form = model.containsAttribute("form") ? (BuyOrderForm) model.getAttribute("form") : new BuyOrderForm();
        if (symbol != null && !symbol.isBlank()) {
            form.setSymbol(symbol);
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", form);
        }

        // Resolve current user position for selected symbol and current price
        String username = principal != null ? principal.getName() : null;
        PortfolioRowView currentPosition = null;
        InstrumentPriceDto selectedInstrument = null;
        if (symbol != null && !symbol.isBlank()) {
            for (InstrumentPriceDto ins : instruments) {
                if (ins != null && symbol.equalsIgnoreCase(ins.getSymbol())) {
                    selectedInstrument = ins;
                    break;
                }
            }
            try {
                if (username != null) {
                    List<PortfolioRowView> rows = portfolioService.getUserPortfolio(username);
                    currentPosition = rows.stream()
                            .filter(r -> r.getSymbol() != null && r.getSymbol().equalsIgnoreCase(symbol))
                            .findFirst()
                            .orElse(null);
                }
            } catch (Exception ex) {
                log.warn("Unable to load current position for user={}, symbol={}: {}", username, symbol, ex.getMessage());
            }
        }

        model.addAttribute("instruments", instruments);
        model.addAttribute("selectedSymbol", symbol);
        model.addAttribute("currentPrice", selectedInstrument != null ? selectedInstrument.getLastPrice() : null);
        model.addAttribute("currentPosition", currentPosition);
        return "trade-buy";
    }

    @PostMapping("/buy")
    public String buySubmit(@Valid @ModelAttribute("form") BuyOrderForm form,
                            BindingResult bindingResult,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Reload instruments on validation error and show the form again
            List<InstrumentPriceDto> instruments = Collections.emptyList();
            try {
                instruments = tradeService.listInstruments();
            } catch (Exception ex) {
                log.error("Error fetching instruments on validation failure", ex);
                model.addAttribute("error", "Failed to load instruments: " + ex.getMessage());
            }
            model.addAttribute("instruments", instruments);
            model.addAttribute("pageTitle", "Buy");
            return "trade-buy";
        }
        try {
            String username = principal != null ? principal.getName() : null;
            log.info("Executing BUY for user={}, symbol={}, qty={}", username, form.getSymbol(), form.getQuantity());
            tradeService.buy(username, form.getSymbol(), form.getQuantity());
            redirectAttributes.addFlashAttribute("success", "Buy order executed successfully for " + form.getQuantity() + " shares of " + form.getSymbol());
            return "redirect:/portfolio";
        } catch (Exception ex) {
            log.error("Error executing buy order", ex);
            model.addAttribute("error", ex.getMessage());
            // Reload instruments and return to page
            List<InstrumentPriceDto> instruments = Collections.emptyList();
            try {
                instruments = tradeService.listInstruments();
            } catch (Exception loadEx) {
                log.error("Error fetching instruments after buy failure", loadEx);
            }
            model.addAttribute("instruments", instruments);
            model.addAttribute("pageTitle", "Buy");
            return "trade-buy";
        }
    }

    @GetMapping("/sell")
    public String sell(@RequestParam(name = "symbol", required = false) String symbol,
                       Principal principal,
                       Model model) {
        model.addAttribute("pageTitle", "Sell");
        String username = principal != null ? principal.getName() : null;
        List<PortfolioRowView> positions = Collections.emptyList();
        try {
            positions = portfolioService.getUserPortfolio(username);
        } catch (Exception ex) {
            log.error("Error loading positions for sell page", ex);
            model.addAttribute("error", "Failed to load positions: " + ex.getMessage());
        }
        // Prepare form (pre-select symbol if provided)
        SellOrderForm form = model.containsAttribute("form") ? (SellOrderForm) model.getAttribute("form") : new SellOrderForm();
        if (symbol != null && !symbol.isBlank()) {
            form.setSymbol(symbol);
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", form);
        }

        // Current price and current position for the selected symbol
        InstrumentPriceDto selectedInstrument = null;
        if (symbol != null && !symbol.isBlank()) {
            try {
                List<InstrumentPriceDto> instruments = tradeService.listInstruments();
                for (InstrumentPriceDto ins : instruments) {
                    if (ins != null && symbol.equalsIgnoreCase(ins.getSymbol())) {
                        selectedInstrument = ins;
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("Unable to load current price for symbol {}: {}", symbol, e.getMessage());
            }
        }

        model.addAttribute("positions", positions);
        model.addAttribute("selectedSymbol", symbol);
        model.addAttribute("currentPosition", positions.stream()
                .filter(r -> r.getSymbol() != null && symbol != null && r.getSymbol().equalsIgnoreCase(symbol))
                .findFirst().orElse(null));
        model.addAttribute("currentPrice", selectedInstrument != null ? selectedInstrument.getLastPrice() : null);
        return "trade-sell";
    }

    @PostMapping("/sell")
    public String sellSubmit(@Valid @ModelAttribute("form") SellOrderForm form,
                             BindingResult bindingResult,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String username = principal != null ? principal.getName() : null;
        if (bindingResult.hasErrors()) {
            // reload positions and return
            List<PortfolioRowView> positions = Collections.emptyList();
            try { positions = portfolioService.getUserPortfolio(username); } catch (Exception ignored) {}
            model.addAttribute("positions", positions);
            model.addAttribute("pageTitle", "Sell");
            return "trade-sell";
        }
        try {
            log.info("Executing SELL for user={}, symbol={}, qty={}", username, form.getSymbol(), form.getQuantity());
            tradeService.sell(username, form.getSymbol(), form.getQuantity());
            redirectAttributes.addFlashAttribute("success", "Sell order executed successfully for " + form.getQuantity() + " shares of " + form.getSymbol());
            return "redirect:/portfolio";
        } catch (Exception ex) {
            log.error("Error executing sell order", ex);
            model.addAttribute("error", ex.getMessage());
            List<PortfolioRowView> positions = Collections.emptyList();
            try { positions = portfolioService.getUserPortfolio(username); } catch (Exception ignored) {}
            model.addAttribute("positions", positions);
            model.addAttribute("pageTitle", "Sell");
            return "trade-sell";
        }
    }
}
