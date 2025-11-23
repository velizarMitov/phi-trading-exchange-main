package com.phitrading.exchange.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuyOrderForm {
    @NotBlank
    private String symbol;

    @Min(1)
    private long quantity;
}
