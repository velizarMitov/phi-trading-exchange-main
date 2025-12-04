package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.domain.entity.Order;
import com.phitrading.exchange.domain.entity.PortfolioPosition;
import com.phitrading.exchange.domain.entity.UserAccount;
import com.phitrading.exchange.domain.repository.OrderRepository;
import com.phitrading.exchange.domain.repository.PortfolioPositionRepository;
import com.phitrading.exchange.domain.repository.UserAccountRepository;
import com.phitrading.exchange.domain.service.ProfileService;
import com.phitrading.exchange.integration.PricingServiceClient;
import com.phitrading.exchange.integration.dto.InstrumentPriceDto;
import com.phitrading.exchange.web.dto.UpdateProfileRequest;
import com.phitrading.exchange.web.dto.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserAccountRepository userAccountRepository;
    private final OrderRepository orderRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final PricingServiceClient pricingServiceClient;

    public ProfileServiceImpl(UserAccountRepository userAccountRepository,
                              OrderRepository orderRepository,
                              PortfolioPositionRepository portfolioPositionRepository,
                              PricingServiceClient pricingServiceClient) {
        this.userAccountRepository = userAccountRepository;
        this.orderRepository = orderRepository;
        this.portfolioPositionRepository = portfolioPositionRepository;
        this.pricingServiceClient = pricingServiceClient;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal cash = safeMoney(user.getCashBalance());

        long totalTrades = orderRepository.countByUser_UsernameAndStatus(username, Order.OrderStatus.EXECUTED);
        long openPositions = portfolioPositionRepository.countByUser_Username(username);

        // Market value of portfolio using current prices from pricing service
        List<PortfolioPosition> positions = portfolioPositionRepository.findAllByUser_Username(username);
        BigDecimal portfolioMarketValue = positions.stream()
                .map(p -> multiply(safePrice(getCurrentPriceSafe(p.getSymbol())), BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal accountValue = cash.add(portfolioMarketValue).setScale(2, RoundingMode.HALF_UP);

        LocalDate memberSince = user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : null;

        List<String> roles = resolveCurrentUserRoles();

        UserProfileDto dto = UserProfileDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .memberSince(memberSince)
                .roles(roles)
                .cashBalance(cash)
                .totalTrades(totalTrades)
                .openPositions(openPositions)
                .accountValue(accountValue)
                .build();

        log.info("Profile loaded for user={}, trades={}, positions={}, accountValue={}", username, totalTrades, openPositions, accountValue);
        return dto;
    }

    @Override
    @Transactional
    public void updateProfile(String username, UpdateProfileRequest request) {
        Objects.requireNonNull(request, "UpdateProfileRequest must not be null");

        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String newEmail = request.getEmail();
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        userAccountRepository.findByEmail(newEmail)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new IllegalArgumentException("Email is already in use"); });

        user.setEmail(newEmail);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userAccountRepository.save(user);

        log.info("Profile updated for user={}, newEmail={}", username, newEmail);
    }

    private List<String> resolveCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private BigDecimal getCurrentPriceSafe(String symbol) {
        try {
            InstrumentPriceDto priceDto = pricingServiceClient.getCurrentPrice(symbol);
            return priceDto != null && priceDto.getLastPrice() != null ? priceDto.getLastPrice() : BigDecimal.ZERO;
        } catch (Exception ex) {
            log.warn("Failed to fetch price for symbol {}: {}", symbol, ex.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal safeMoney(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : v.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal safePrice(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
