package com.sadoon.cbotback.exceptions.exchange;

import com.sadoon.cbotback.exceptions.CustomException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import reactor.core.publisher.Sinks;

public class ExchangeConnectionException extends CustomException {
    private ExchangeName exchange;

    public ExchangeConnectionException(ExchangeName exchange, Sinks.EmitResult result) {
        super(String.format("Could not send message to %1s due to: %2s", exchange, result));
        this.exchange = exchange;
    }

    public ExchangeName getExchange() {
        return exchange;
    }
}
