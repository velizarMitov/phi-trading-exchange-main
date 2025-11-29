package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.common.exception.SymbolInUseException;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.service.AdminSymbolService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.CreateInstrumentRequest;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.AdminSymbolDeleteResult;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminSymbolServiceImpl implements AdminSymbolService {

    private static final Logger log = LoggerFactory.getLogger(AdminSymbolServiceImpl.class);

    private final PricingServiceClient pricingServiceClient;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final OrderRepository orderRepository;

    public AdminSymbolServiceImpl(PricingServiceClient pricingServiceClient,
                                  PortfolioPositionRepository portfolioPositionRepository,
                                  OrderRepository orderRepository) {
        this.pricingServiceClient = pricingServiceClient;
        this.portfolioPositionRepository = portfolioPositionRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public InstrumentPriceDto addSymbol(String symbol, String name, BigDecimal initialPrice) {
        CreateInstrumentRequest request = new CreateInstrumentRequest(symbol, name, initialPrice);
        log.info("Creating instrument in pricing service: symbol={}, name={}, initialPrice={}", symbol, name, initialPrice);
        try {
            InstrumentPriceDto created = pricingServiceClient.createOrUpdateInstrument(request);
            log.info("Created instrument in pricing service: symbol={}, lastPrice={}", created.getSymbol(), created.getLastPrice());
            return created;
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            log.error("Failed to create instrument in pricing service. status={}, body={}", e.status(), responseBody);
            throw e;
        }
    }

    @Override
    public List<InstrumentPriceDto> getAllInstruments() {
        log.info("Fetching all instruments from pricing service");
        return pricingServiceClient.getAllInstruments();
    }

    @Override
    public void removeSymbol(String symbol) {
        String normalized = symbol != null ? symbol.trim().toUpperCase() : null;
        log.info("Attempting to remove symbol: {}", normalized);

        boolean hasPositions = false;
        boolean hasOrders = false;
        try {
            // Add repository methods if missing: existsBySymbol
            hasPositions = portfolioPositionRepository instanceof Object && existsPositionBySymbol(normalized);
            hasOrders = orderRepository instanceof Object && existsOrderBySymbol(normalized);
        } catch (Exception e) {
            log.warn("Symbol usage check encountered an issue; proceeding with best-effort checks: {}", e.getMessage());
        }

        if (hasPositions || hasOrders) {
            log.warn("Symbol {} is in use (positions: {}, orders: {}), refusing deletion", normalized, hasPositions, hasOrders);
            throw new SymbolInUseException(normalized);
        }

        log.info("Removing symbol from pricing service: {}", normalized);
        pricingServiceClient.deleteInstrument(normalized);
        log.info("Symbol {} removed from pricing service", normalized);
        // If a local TradableSymbol entity exists, delete here as well (not present in current codebase)
    }

    @Override
    public AdminSymbolDeleteResult deleteSymbolSafely(String symbol) {
        String normalized = symbol != null ? symbol.trim().toUpperCase() : null;
        AdminSymbolDeleteResult result = new AdminSymbolDeleteResult();
        result.setSymbol(normalized);

        log.info("Admin requested safe delete for symbol: {}", normalized);

        // Gather portfolio positions for the symbol
        List<com.phitrading.exchange.domain.entity.PortfolioPosition> positions = new ArrayList<>();
        try {
            positions = portfolioPositionRepository.findBySymbol(normalized);
        } catch (Exception ex) {
            // Fallback: scan if custom method missing (should exist)
            log.debug("findBySymbol not available directly, performing fallback scan: {}", ex.getMessage());
            positions = portfolioPositionRepository.findAll().stream()
                    .filter(p -> normalized != null && normalized.equalsIgnoreCase(p.getSymbol()))
                    .collect(Collectors.toList());
        }

        long positionsCount = positions.size();
        long ordersCount;
        try {
            ordersCount = orderRepository.countBySymbol(normalized);
        } catch (Exception ex) {
            log.debug("countBySymbol not available for orders, performing fallback scan: {}", ex.getMessage());
            ordersCount = orderRepository.findAll().stream()
                    .filter(o -> normalized != null && normalized.equalsIgnoreCase(o.getSymbol()))
                    .count();
        }

        if (positionsCount > 0) {
            List<String> holders = positions.stream()
                    .map(p -> p.getUser() != null ? p.getUser().getUsername() : "unknown")
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            result.setDeleted(false);
            result.setHolderUsernames(holders);
            result.setPositionsCount(positionsCount);
            result.setOrdersCount(ordersCount);
            String msg = String.format(
                    "Cannot delete symbol %s: there are %d portfolio positions and %d orders for users %s. Close positions first.",
                    normalized, positionsCount, ordersCount, holders);
            result.setMessage(msg);
            log.warn(msg);
            return result;
        }

        // No positions: allow deletion (orders could be historical)
        try {
            log.info("No positions for symbol {}. Proceeding to delete via pricing service.", normalized);
            pricingServiceClient.deleteInstrument(normalized);
            // If local symbol entity existed, delete here as well.
            result.setDeleted(true);
            result.setPositionsCount(0);
            result.setOrdersCount(ordersCount);
            result.setHolderUsernames(List.of());
            String msg = String.format("Symbol %s was removed successfully.", normalized);
            result.setMessage(msg);
            log.info(msg);
            return result;
        } catch (Exception ex) {
            // Deletion failed on remote service; present as non-deleted with error message
            String msg = String.format("Failed to remove symbol %s: %s", normalized, ex.getMessage());
            log.error(msg, ex);
            result.setDeleted(false);
            result.setPositionsCount(0);
            result.setOrdersCount(ordersCount);
            result.setHolderUsernames(List.of());
            result.setMessage(msg);
            return result;
        }
    }

    // Helper wrappers to keep compile-time safety with repository methods that may not yet exist
    private boolean existsPositionBySymbol(String symbol) {
        try {
            var method = portfolioPositionRepository.getClass().getMethod("existsBySymbol", String.class);
            Object res = method.invoke(portfolioPositionRepository, symbol);
            return res instanceof Boolean ? (Boolean) res : false;
        } catch (ReflectiveOperationException e) {
            // Method not present → fallback: scan
            return portfolioPositionRepository.findAll().stream().anyMatch(p -> symbol.equalsIgnoreCase(p.getSymbol()));
        }
    }

    private boolean existsOrderBySymbol(String symbol) {
        try {
            var method = orderRepository.getClass().getMethod("existsBySymbol", String.class);
            Object res = method.invoke(orderRepository, symbol);
            return res instanceof Boolean ? (Boolean) res : false;
        } catch (ReflectiveOperationException e) {
            // Method not present → fallback: scan (could be heavy, but acceptable for admin op)
            return orderRepository.findAll().stream().anyMatch(o -> symbol.equalsIgnoreCase(o.getSymbol()));
        }
    }
}

