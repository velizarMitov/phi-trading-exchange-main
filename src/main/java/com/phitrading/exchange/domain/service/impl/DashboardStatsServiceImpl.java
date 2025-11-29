package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.DashboardStatsService;
import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.web.dto.DashboardStats;
import com.phitrading.exchange.web.dto.PortfolioView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsServiceImpl.class);

    private final UserAccountRepository userRepo;
    private final PortfolioPositionRepository positionRepo;
    private final OrderRepository orderRepo;
    private final PortfolioService portfolioService;
    private final StatsCache cache;

    public DashboardStatsServiceImpl(UserAccountRepository userRepo,
                                     PortfolioPositionRepository positionRepo,
                                     OrderRepository orderRepo,
                                     PortfolioService portfolioService,
                                     StatsCache cache) {
        this.userRepo = userRepo;
        this.positionRepo = positionRepo;
        this.orderRepo = orderRepo;
        this.portfolioService = portfolioService;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats computeStatsForUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when computing stats.");
        }

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal cash = zero(user.getCashBalance());

        // Portfolio totals using existing service (already pulls live prices via Feign)
        PortfolioView pv = portfolioService.getUserPortfolioView(username);
        BigDecimal portfolioValue = zero(pv.getTotalCurrentValue());
        BigDecimal accountValue = cash.add(portfolioValue).setScale(2, RoundingMode.HALF_UP);

        long positionsCount = positionRepo.countByUser_Username(username);
        long executedOrders = orderRepo.countByUser_UsernameAndStatus(username, com.phitrading.exchange.domain.entity.Order.OrderStatus.EXECUTED);

        DashboardStats stats = DashboardStats.builder()
                .cashBalance(money(cash))
                .totalPortfolioValue(money(portfolioValue))
                .totalAccountValue(money(accountValue))
                .realizedPl(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .positionsCount(positionsCount)
                .executedOrdersCount(executedOrders)
                .lastUpdated(LocalDateTime.now())
                .build();

        log.info("Computed dashboard stats for user={}, cash={}, portfolioValue={}, accountValue={}, positions={}, execOrders={}",
                username, stats.getCashBalance(), stats.getTotalPortfolioValue(), stats.getTotalAccountValue(),
                positionsCount, executedOrders);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStatsForUser(String username) {
        DashboardStats cached = cache.get(username);
        if (cached != null) {
            return cached;
        }
        DashboardStats computed = computeStatsForUser(username);
        cache.put(username, computed);
        return computed;
    }

    private static BigDecimal zero(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static BigDecimal money(BigDecimal v) { return zero(v).setScale(2, RoundingMode.HALF_UP); }

    /** Simple in-memory cache bean */
    @Component
    public static class StatsCache {
        private final java.util.concurrent.ConcurrentMap<String, DashboardStats> map = new java.util.concurrent.ConcurrentHashMap<>();

        public DashboardStats get(String username) {
            return map.get(username);
        }

        public void put(String username, DashboardStats stats) {
            if (username != null && stats != null) {
                map.put(username, stats);
            }
        }

        public void clear() { map.clear(); }
        public int size() { return map.size(); }
        public java.util.Set<String> keys() { return map.keySet(); }
    }
}
