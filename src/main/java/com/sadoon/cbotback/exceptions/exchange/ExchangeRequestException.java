package com.sadoon.cbotback.exceptions.exchange;

import com.sadoon.cbotback.exceptions.CustomException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;

public class ExchangeRequestException extends CustomException {
    private ExchangeName exchange;

    public ExchangeRequestException(ExchangeName exchange, String error) {
        super(String.format("%1s responded with: %2s", exchange, error));
        this.exchange = exchange;
    }

    public ExchangeName getExchange() {
        return exchange;
    }
}