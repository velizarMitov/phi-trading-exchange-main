package com.phitrading.exchange.config.scheduling;

import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.DashboardStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DashboardStatsScheduler {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsScheduler.class);

    private final UserAccountRepository userAccountRepository;
    private final DashboardStatsService dashboardStatsService;

    public DashboardStatsScheduler(UserAccountRepository userAccountRepository,
                                   DashboardStatsService dashboardStatsService) {
        this.userAccountRepository = userAccountRepository;
        this.dashboardStatsService = dashboardStatsService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void refreshAllUserStats() {
        var users = userAccountRepository.findAll();
        int refreshed = 0;
        for (var u : users) {
            try {
                dashboardStatsService.computeStatsForUser(u.getUsername());
                // store in cache through getStatsForUser ensures put, or explicit cache put in service compute step
                dashboardStatsService.getStatsForUser(u.getUsername());
                refreshed++;
            } catch (Exception e) {
                log.warn("Failed to refresh stats for user={}: {}", u.getUsername(), e.getMessage());
            }
        }
        log.info("DashboardStatsScheduler refreshed stats for {} users", refreshed);
    }
}
