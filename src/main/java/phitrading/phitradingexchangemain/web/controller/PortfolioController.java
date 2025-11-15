package phitrading.phitradingexchangemain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class PortfolioController {

    @GetMapping("/portfolio")
    public String portfolio(Model model) {
        model.addAttribute("pageTitle", "Portfolio");
        model.addAttribute("positions", Collections.emptyList());
        return "portfolio";
    }
}
