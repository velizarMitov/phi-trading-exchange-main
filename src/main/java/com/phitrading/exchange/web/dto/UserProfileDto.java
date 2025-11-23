package com.phitrading.exchange.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class UserProfileDto {
    String username;
    String email;
    LocalDate memberSince;
    List<String> roles;

    BigDecimal cashBalance;
    long totalTrades;
    long openPositions;
    BigDecimal accountValue;
}
