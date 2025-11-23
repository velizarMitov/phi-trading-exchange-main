package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.DashboardView;

public interface DashboardService {
    DashboardView getDashboard(String username);
}
