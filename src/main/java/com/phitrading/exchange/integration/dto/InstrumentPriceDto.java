package com.phitrading.exchange.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing instrument price information from the pricing service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentPriceDto {
    // Pricing service uses UUID for IDs
    private UUID id;
    private String symbol;
    private String name;
    private BigDecimal lastPrice;
    private BigDecimal previousClose;
    // Map JSON property "updatedAt" from the pricing service
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

