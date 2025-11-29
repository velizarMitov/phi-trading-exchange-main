package com.phitrading.exchange.common.util;

import com.phitrading.exchange.common.CurrencyCode;
import com.phitrading.exchange.domain.service.CurrencyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Stores the user's selected display currency in the HTTP session.
 */
@Component
public class UserCurrencyContext {

    private static final String SESSION_KEY = "ACTIVE_CURRENCY";

    private final HttpSession session;
    private final CurrencyService currencyService;

    public UserCurrencyContext(HttpSession session, CurrencyService currencyService) {
        this.session = session;
        this.currencyService = currencyService;
    }

    public CurrencyCode getCurrentCurrency() {
        Object v = session.getAttribute(SESSION_KEY);
        if (v instanceof CurrencyCode code) {
            return code;
        }
        return currencyService.getDefaultCurrency();
    }

    public void setCurrentCurrency(CurrencyCode code) {
        if (code == null) return;
        session.setAttribute(SESSION_KEY, code);
    }
}
