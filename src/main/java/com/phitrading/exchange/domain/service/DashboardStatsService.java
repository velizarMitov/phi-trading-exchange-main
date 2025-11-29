package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.DashboardStats;

public interface DashboardStatsService {
    DashboardStats computeStatsForUser(String username);

    DashboardStats getStatsForUser(String username);
}
