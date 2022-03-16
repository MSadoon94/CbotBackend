package com.sadoon.cbotback.exceptions.exchange;

import com.sadoon.cbotback.exceptions.CustomException;
import com.sadoon.cbotback.exchange.ExchangeNames;

public class ExchangeRequestException extends CustomException {

    public ExchangeRequestException(ExchangeNames exchange, String error) {
        super(String.format("%1s responded with: %2s", exchange, error));
    }
}