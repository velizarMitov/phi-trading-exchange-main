package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.web.dto.PortfolioRowView;
import com.phitrading.exchange.web.dto.PortfolioView;

import java.util.List;

public interface PortfolioService {
    List<PortfolioRowView> getUserPortfolio(String username);

    PortfolioView getUserPortfolioView(String username);
}
