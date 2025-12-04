package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.entity.Order;
import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.DashboardService;
import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.web.dto.DashboardView;
import com.phitrading.exchange.web.dto.PortfolioRowView;
import com.phitrading.exchange.web.dto.PortfolioView;
import com.phitrading.exchange.web.dto.RecentOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final PortfolioService portfolioService;
    private final OrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;

    public DashboardServiceImpl(PortfolioService portfolioService,
                                OrderRepository orderRepository,
                                UserAccountRepository userAccountRepository) {
        this.portfolioService = portfolioService;
        this.orderRepository = orderRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardView getDashboard(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when loading dashboard.");
        }

        // Load user for cash balance (safe variant to avoid NonUniqueResultException on duplicates)
        UserAccount user = userAccountRepository.findFirstByUsernameOrderByUpdatedAtDesc(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        BigDecimal cash = defaultZero(user.getCashBalance());

        // Build portfolio view (totals + rows have live prices)
        PortfolioView portfolio = portfolioService.getUserPortfolioView(username);
        BigDecimal totalCost = defaultZero(portfolio.getTotalCost());
        BigDecimal totalCurrentValue = defaultZero(portfolio.getTotalCurrentValue());
        BigDecimal totalPnlAbs = defaultZero(portfolio.getTotalPnlAbs());
        BigDecimal totalPnlPct = defaultZero(portfolio.getTotalPnlPct());

        BigDecimal totalAccountValue = cash.add(totalCurrentValue).setScale(2, RoundingMode.HALF_UP);

        // Top 3 positions by current value (quantity * currentPrice)
        List<PortfolioRowView> topPositions = portfolio.getRows().stream()
                .sorted(Comparator.comparing((PortfolioRowView r) ->
                        safeMoney(r.getCurrentPrice()).multiply(defaultZero(r.getQuantity())))
                        .reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Last 3 executed orders
        List<RecentOrderView> recentOrders = orderRepository
                .findTop3ByUser_UsernameAndStatusOrderByExecutedAtDesc(username, Order.OrderStatus.EXECUTED)
                .stream()
                .map(o -> RecentOrderView.builder()
                        .symbol(o.getSymbol())
                        .side(o.getSide() != null ? o.getSide().name() : null)
                        .quantity(BigDecimal.valueOf(o.getQuantity()))
                        .executionPrice(o.getExecutionPrice())
                        .executedAt(o.getExecutedAt())
                        .build())
                .collect(Collectors.toList());

        DashboardView view = DashboardView.builder()
                .cashBalance(safeMoney(cash))
                .totalCost(safeMoney(totalCost))
                .totalCurrentValue(safeMoney(totalCurrentValue))
                .totalAccountValue(safeMoney(totalAccountValue))
                .totalPnlAbs(safeMoney(totalPnlAbs))
                .totalPnlPct(scalePct(totalPnlPct))
                .topPositions(topPositions)
                .recentOrders(recentOrders)
                .build();

        log.info("Dashboard computed for user={}, cash={}, portfolioValue={}, accountValue={}, topPositions={}, recentOrders={}",
                username, view.getCashBalance(), view.getTotalCurrentValue(), view.getTotalAccountValue(),
                view.getTopPositions().size(), view.getRecentOrders().size());

        return view;
    }

    private static BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal safeMoney(BigDecimal v) {
        return defaultZero(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scalePct(BigDecimal v) {
        return defaultZero(v).setScale(2, RoundingMode.HALF_UP);
    }
}
