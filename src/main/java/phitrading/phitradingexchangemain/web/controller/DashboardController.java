package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.common.CurrencyCode;
import com.phitrading.exchange.common.util.UserCurrencyContext;
import com.phitrading.exchange.domain.service.CurrencyService;
import com.phitrading.exchange.domain.service.DashboardService;
import com.phitrading.exchange.web.dto.DashboardView;
import com.phitrading.exchange.domain.service.DashboardStatsService;
import com.phitrading.exchange.web.dto.DashboardStats;
import com.phitrading.exchange.web.dto.PortfolioRowView;
import com.phitrading.exchange.web.dto.RecentOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final CurrencyService currencyService;
    private final DashboardStatsService dashboardStatsService;
    private final UserCurrencyContext userCurrencyContext;

    public DashboardController(DashboardService dashboardService,
                               CurrencyService currencyService,
                               UserCurrencyContext userCurrencyContext,
                               DashboardStatsService dashboardStatsService) {
        this.dashboardService = dashboardService;
        this.currencyService = currencyService;
        this.userCurrencyContext = userCurrencyContext;
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal,
                            @RequestParam(name = "currency", required = false) String currencyCode,
                            Model model) {
        String username = principal != null ? principal.getName() : null;
        log.info("Loading dashboard for user={}", username);
        // Handle currency switch (optional param)
        if (currencyCode != null) {
            try {
                CurrencyCode requested = CurrencyCode.valueOf(currencyCode.toUpperCase());
                userCurrencyContext.setCurrentCurrency(requested);
            } catch (IllegalArgumentException ignored) {
                // ignore invalid values
            }
        }

        CurrencyCode current = userCurrencyContext.getCurrentCurrency();

        // Load base dashboard (USD assumed)
        DashboardView base = dashboardService.getDashboard(username);

        // Convert monetary figures for display to current currency
        DashboardView converted = convertDashboard(base, current);

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("dashboard", converted);
        model.addAttribute("activeCurrency", current);
        model.addAttribute("supportedCurrencies", currencyService.getSupportedCurrencies());
        // Add precomputed (or on-demand) aggregated stats
        DashboardStats stats = dashboardStatsService.getStatsForUser(username);
        model.addAttribute("dashboardStats", stats);
        return "dashboard";
    }

    private DashboardView convertDashboard(DashboardView base, CurrencyCode target) {
        CurrencyCode from = CurrencyCode.USD; // base currency
        BigDecimal cash = currencyService.convert(base.getCashBalance(), from, target);
        BigDecimal totalCost = currencyService.convert(base.getTotalCost(), from, target);
        BigDecimal totalCurrent = currencyService.convert(base.getTotalCurrentValue(), from, target);
        BigDecimal totalAccount = currencyService.convert(base.getTotalAccountValue(), from, target);
        BigDecimal pnlAbs = currencyService.convert(base.getTotalPnlAbs(), from, target);
        BigDecimal pnlPct = base.getTotalPnlPct() == null ? BigDecimal.ZERO : base.getTotalPnlPct().setScale(2, RoundingMode.HALF_UP);

        List<PortfolioRowView> convertedRows = base.getTopPositions() == null ? List.of() : base.getTopPositions().stream()
                .map(r -> PortfolioRowView.builder()
                        .symbol(r.getSymbol())
                        .name(r.getName())
                        .quantity(r.getQuantity())
                        .averagePrice(currencyService.convert(r.getAveragePrice(), from, target))
                        .currentPrice(currencyService.convert(r.getCurrentPrice(), from, target))
                        .pnlAbs(currencyService.convert(r.getPnlAbs(), from, target))
                        .pnlPct(r.getPnlPct())
                        .build())
                .collect(Collectors.toList());

        List<RecentOrderView> convertedOrders = base.getRecentOrders() == null ? List.of() : base.getRecentOrders().stream()
                .map(o -> RecentOrderView.builder()
                        .symbol(o.getSymbol())
                        .side(o.getSide())
                        .quantity(o.getQuantity())
                        .executionPrice(currencyService.convert(o.getExecutionPrice(), from, target))
                        .executedAt(o.getExecutedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardView.builder()
                .cashBalance(cash)
                .totalCost(totalCost)
                .totalCurrentValue(totalCurrent)
                .totalAccountValue(totalAccount)
                .totalPnlAbs(pnlAbs)
                .totalPnlPct(pnlPct)
                .topPositions(convertedRows)
                .recentOrders(convertedOrders)
                .activeCurrency(target)
                .supportedCurrencies(currencyService.getSupportedCurrencies())
                .build();
    }
}
