package com.phitrading.exchange.domain.service.impl;

import com.phitrading.exchange.common.CurrencyCode;
import com.phitrading.exchange.domain.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceImpl.class);

    // Base currency is USD. Values are how much 1 USD equals in the target currency.
    private final Map<CurrencyCode, BigDecimal> usdToTarget = new EnumMap<>(CurrencyCode.class);

    public CurrencyServiceImpl() {
        // Demo rates; easy to adjust later or wire from config
        usdToTarget.put(CurrencyCode.USD, BigDecimal.ONE);
        usdToTarget.put(CurrencyCode.EUR, new BigDecimal("0.92"));
        usdToTarget.put(CurrencyCode.BGN, new BigDecimal("1.80"));
    }

    @Override
    public CurrencyCode getDefaultCurrency() {
        return CurrencyCode.USD;
    }

    @Override
    public List<CurrencyCode> getSupportedCurrencies() {
        return List.of(CurrencyCode.USD, CurrencyCode.EUR, CurrencyCode.BGN);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, CurrencyCode from, CurrencyCode to) {
        if (amount == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (from == null) from = getDefaultCurrency();
        if (to == null) to = getDefaultCurrency();
        if (from == to) return amount.setScale(2, RoundingMode.HALF_UP);

        // Convert via USD base
        BigDecimal inUsd;
        if (from == CurrencyCode.USD) {
            inUsd = amount;
        } else {
            // amount_in_usd = amount / (1 USD in FROM)
            BigDecimal rateFrom = usdToTarget.getOrDefault(from, BigDecimal.ONE);
            if (rateFrom.compareTo(BigDecimal.ZERO) == 0) rateFrom = BigDecimal.ONE;
            inUsd = amount.divide(rateFrom, 8, RoundingMode.HALF_UP);
        }

        BigDecimal rateTo = usdToTarget.getOrDefault(to, BigDecimal.ONE);
        BigDecimal result = inUsd.multiply(rateTo);
        return result.setScale(2, RoundingMode.HALF_UP);
    }
}
