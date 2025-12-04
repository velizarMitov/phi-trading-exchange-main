package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.PortfolioService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.PortfolioRowView;
import com.phitrading.exchange.web.dto.PortfolioView;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioPositionRepository portfolioRepo;
    private final UserAccountRepository userRepo;
    private final PricingServiceClient pricingClient;

    public PortfolioServiceImpl(PortfolioPositionRepository portfolioRepo,
                                UserAccountRepository userRepo,
                                PricingServiceClient pricingClient) {
        this.portfolioRepo = portfolioRepo;
        this.userRepo = userRepo;
        this.pricingClient = pricingClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioRowView> getUserPortfolio(String username) {
        return buildView(username).getRows();
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioView getUserPortfolioView(String username) {
        return buildView(username);
    }

    private PortfolioView buildView(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when loading portfolio.");
        }

        // Ensure user exists (optional but good validation)
        userRepo.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("User not found: " + username));

        List<PortfolioPosition> positions = portfolioRepo.findAllByUser_Username(username);
        List<PortfolioRowView> rows = new ArrayList<>();

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (PortfolioPosition p : positions) {
            String symbol = p.getSymbol();
            long qtyLong = p.getQuantity();
            BigDecimal qty = BigDecimal.valueOf(qtyLong);
            BigDecimal avgPrice = defaultZero(p.getAveragePrice());

            BigDecimal currentPrice = BigDecimal.ZERO;
            String name = null;
            try {
                InstrumentPriceDto dto = pricingClient.getCurrentPrice(symbol);
                if (dto != null && dto.getLastPrice() != null) {
                    currentPrice = dto.getLastPrice();
                }
                name = dto != null ? dto.getName() : null;
            } catch (FeignException ex) {
                log.warn("Failed to get current price for symbol={} from pricing service: status={}, msg={}",
                        symbol, ex.status(), ex.getMessage());
            } catch (Exception e) {
                log.warn("Unexpected error fetching price for symbol={}: {}", symbol, e.getMessage());
            }

            BigDecimal costBasis = avgPrice.multiply(qty);
            BigDecimal currentValue = currentPrice.multiply(qty);
            BigDecimal pnlAbs = currentValue.subtract(costBasis);
            BigDecimal pnlPct = percent(pnlAbs, costBasis);

            totalCost = totalCost.add(costBasis);
            totalCurrentValue = totalCurrentValue.add(currentValue);

            rows.add(PortfolioRowView.builder()
                    .symbol(symbol)
                    .name(name)
                    .quantity(qty)
                    .averagePrice(money(avgPrice))
                    .currentPrice(money(currentPrice))
                    .pnlAbs(money(pnlAbs))
                    .pnlPct(scalePct(pnlPct))
                    .build());
        }

        BigDecimal totalPnlAbs = totalCurrentValue.subtract(totalCost);
        BigDecimal totalPnlPct = percent(totalPnlAbs, totalCost);

        PortfolioView view = PortfolioView.builder()
                .rows(rows)
                .totalCost(money(totalCost))
                .totalCurrentValue(money(totalCurrentValue))
                .totalPnlAbs(money(totalPnlAbs))
                .totalPnlPct(scalePct(totalPnlPct))
                .build();

        log.info("Computed portfolio for user={}, positions={}, totalCost={}, totalValue={}, pnlAbs={}, pnlPct={}",
                username, rows.size(), view.getTotalCost(), view.getTotalCurrentValue(), view.getTotalPnlAbs(), view.getTotalPnlPct());

        return view;
    }

    private static BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal money(BigDecimal v) {
        return defaultZero(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scalePct(BigDecimal v) {
        return defaultZero(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.multiply(BigDecimal.valueOf(100))
                .divide(denominator, 6, RoundingMode.HALF_UP);
    }
}
