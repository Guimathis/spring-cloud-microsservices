package com.guimathis.exchangeservice.exception;

public class ExchangeNotSuportedException extends RuntimeException {
    public ExchangeNotSuportedException(String fromCurrency, String toCurrency) {
        super("Exchange not supported for " + fromCurrency + " to " + toCurrency);
    }
}
