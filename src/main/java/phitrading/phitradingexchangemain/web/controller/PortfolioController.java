package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.web.dto.PortfolioView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class PortfolioController {

    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio")
    public String portfolio(Principal principal, Model model) {
        String username = principal != null ? principal.getName() : null;
        PortfolioView view = portfolioService.getUserPortfolioView(username);
        model.addAttribute("pageTitle", "Portfolio");
        model.addAttribute("portfolio", view);
        log.info("Loading portfolio for user={}, positions={}", username, view.getRows().size());
        return "portfolio";
    }
}
