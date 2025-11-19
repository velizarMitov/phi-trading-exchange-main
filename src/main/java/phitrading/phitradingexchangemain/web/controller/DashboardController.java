package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.DashboardService;
import com.phitrading.exchange.web.dto.DashboardView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String username = principal != null ? principal.getName() : null;
        log.info("Loading dashboard for user={}", username);
        DashboardView dashboard = dashboardService.getDashboard(username);
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("dashboard", dashboard);
        return "dashboard";
    }
}
