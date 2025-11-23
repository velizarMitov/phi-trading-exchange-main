package com.phitrading.exchange.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating an instrument in the pricing service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstrumentRequest {
    @NotBlank
    @Size(min = 1, max = 10)
    private String symbol;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Positive
    private BigDecimal initialPrice;
}

