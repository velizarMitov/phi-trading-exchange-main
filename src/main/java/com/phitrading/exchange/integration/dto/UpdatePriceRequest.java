package com.phitrading.exchange.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating the price of an instrument
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    private BigDecimal newPrice;
}

