package com.phitrading.exchange.common.exception;

public class SymbolInUseException extends RuntimeException {
    public SymbolInUseException(String symbol) {
        super("Cannot remove symbol " + symbol + " because it is used in portfolio positions or orders.");
    }
}
