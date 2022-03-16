package com.sadoon.cbotback.api;

import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.ExchangeNames;

import java.util.Arrays;

public interface KrakenResponse {

    String[] getErrors();

    default void checkErrors(String[] errors) throws ExchangeRequestException {
        if (Arrays.stream(errors).findFirst().isPresent()) {
            throw new ExchangeRequestException(ExchangeNames.KRAKEN, Arrays.toString(errors));
        }
    }

    void setErrors(String[] errors);
}
