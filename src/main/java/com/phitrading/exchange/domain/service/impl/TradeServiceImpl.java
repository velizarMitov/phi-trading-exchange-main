package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.common.exception.InsufficientFundsException;
import com.phitrading.exchange.domain.entity.Order;
import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.TradeService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);

    private final PricingServiceClient pricingServiceClient;
    private final UserAccountRepository userAccountRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final OrderRepository orderRepository;

    public TradeServiceImpl(PricingServiceClient pricingServiceClient,
                            UserAccountRepository userAccountRepository,
                            PortfolioPositionRepository portfolioPositionRepository,
                            OrderRepository orderRepository) {
        this.pricingServiceClient = pricingServiceClient;
        this.userAccountRepository = userAccountRepository;
        this.portfolioPositionRepository = portfolioPositionRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<InstrumentPriceDto> listInstruments() {
        log.info("Fetching all instruments from pricing service (trade)");
        return pricingServiceClient.getAllInstruments();
    }

    @Override
    @Transactional
    public void buy(String username, String symbol, long quantity) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when buying.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        try {
            log.info("Starting BUY operation: user={}, symbol={}, qty={}", username, symbol, quantity);
            InstrumentPriceDto priceDto = pricingServiceClient.getCurrentPrice(symbol);
            BigDecimal price = priceDto.getLastPrice();
            if (price == null) {
                throw new IllegalArgumentException("No price available for symbol: " + symbol);
            }

            UserAccount user = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            BigDecimal qty = BigDecimal.valueOf(quantity);
            BigDecimal cost = price.multiply(qty).setScale(4, RoundingMode.HALF_UP);

            if (user.getCashBalance() == null) {
                user.setCashBalance(BigDecimal.ZERO);
            }
            if (user.getCashBalance().compareTo(cost) < 0) {
                log.error("Insufficient funds for user={}, balance={}, cost={}", username, user.getCashBalance(), cost);
                throw new InsufficientFundsException("Insufficient funds. Required: " + cost + ", Available: " + user.getCashBalance());
            }

            // Create executed order
            Order order = new Order();
            order.setUser(user);
            order.setSymbol(symbol);
            order.setQuantity(quantity);
            order.setSide(Order.OrderSide.BUY);
            order.setStatus(Order.OrderStatus.EXECUTED);
            order.setExecutionPrice(price);
            order.setExecutedAt(LocalDateTime.now());

            // Deduct cash
            user.setCashBalance(user.getCashBalance().subtract(cost));
            user.setUpdatedAt(LocalDateTime.now());
            userAccountRepository.save(user);

            // Update portfolio position
            PortfolioPosition position = portfolioPositionRepository.findByUserAndSymbol(user, symbol)
                    .orElseGet(() -> {
                        PortfolioPosition p = new PortfolioPosition();
                        p.setUser(user);
                        p.setSymbol(symbol);
                        p.setQuantity(0);
                        p.setAveragePrice(BigDecimal.ZERO);
                        return p;
                    });

            long oldQty = position.getQuantity();
            BigDecimal oldAvg = position.getAveragePrice() == null ? BigDecimal.ZERO : position.getAveragePrice();

            long newQty = oldQty + quantity;
            BigDecimal totalCost = oldAvg.multiply(BigDecimal.valueOf(oldQty)).add(price.multiply(qty));
            BigDecimal newAvg = totalCost.divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP);

            position.setQuantity(newQty);
            position.setAveragePrice(newAvg);
            position.setUpdatedAt(LocalDateTime.now());
            portfolioPositionRepository.save(position);

            orderRepository.save(order);

            log.info("Executed BUY order: user={}, symbol={}, qty={}, price={}, newQty={}, newAvg={}",
                    username, symbol, quantity, price, newQty, newAvg);
        } catch (FeignException e) {
            log.error("Pricing service error while buying symbol={}: status={}, body={}", symbol, e.status(), e.contentUTF8());
            throw e;
        }
    }

    @Override
    @Transactional
    public void sell(String username, String symbol, long quantity) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank when selling.");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be null or blank when selling.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        try {
            log.info("Starting SELL operation: user={}, symbol={}, qty={}", username, symbol, quantity);

            // Load current price
            InstrumentPriceDto priceDto = pricingServiceClient.getCurrentPrice(symbol);
            BigDecimal price = priceDto.getLastPrice();
            if (price == null) {
                throw new IllegalArgumentException("No price available for symbol: " + symbol);
            }

            // Load user and position
            UserAccount user = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            PortfolioPosition position = portfolioPositionRepository.findByUserAndSymbol(user, symbol)
                    .orElseThrow(() -> new IllegalArgumentException("Position not found for symbol: " + symbol));

            long available = position.getQuantity();
            if (available < quantity) {
                throw new IllegalArgumentException("Insufficient quantity. Available: " + available + ", Requested: " + quantity);
            }

            BigDecimal qty = BigDecimal.valueOf(quantity);
            BigDecimal avg = position.getAveragePrice() == null ? BigDecimal.ZERO : position.getAveragePrice();
            BigDecimal proceeds = price.multiply(qty).setScale(4, RoundingMode.HALF_UP);
            BigDecimal costBasis = avg.multiply(qty).setScale(4, RoundingMode.HALF_UP);
            BigDecimal realizedPnl = proceeds.subtract(costBasis).setScale(4, RoundingMode.HALF_UP);

            // Create executed SELL order
            Order order = new Order();
            order.setUser(user);
            order.setSymbol(symbol);
            order.setQuantity(quantity);
            order.setSide(Order.OrderSide.SELL);
            order.setStatus(Order.OrderStatus.EXECUTED);
            order.setExecutionPrice(price);
            order.setExecutedAt(LocalDateTime.now());
            order.setRealizedPnl(realizedPnl);

            // Increase user cash balance
            if (user.getCashBalance() == null) {
                user.setCashBalance(BigDecimal.ZERO);
            }
            user.setCashBalance(user.getCashBalance().add(proceeds));
            user.setUpdatedAt(LocalDateTime.now());
            userAccountRepository.save(user);

            // Update or delete position
            long newQty = available - quantity;
            if (newQty == 0) {
                portfolioPositionRepository.delete(position);
            } else {
                position.setQuantity(newQty);
                position.setUpdatedAt(LocalDateTime.now());
                portfolioPositionRepository.save(position);
            }

            orderRepository.save(order);

            log.info("Executed SELL order: user={}, symbol={}, qty={}, price={}, realizedPnl={}, remainingQty={}",
                    username, symbol, quantity, price, realizedPnl, newQty);
        } catch (FeignException e) {
            log.error("Pricing service error while selling symbol={}: status={}, body={}", symbol, e.status(), e.contentUTF8());
            throw e;
        }
    }
}
