package com.phitrading.exchange.domain.service;

import com.phitrading.exchange.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

public interface CurrencyService {
    CurrencyCode getDefaultCurrency();

    List<CurrencyCode> getSupportedCurrencies();

    BigDecimal convert(BigDecimal amount, CurrencyCode from, CurrencyCode to);
}
