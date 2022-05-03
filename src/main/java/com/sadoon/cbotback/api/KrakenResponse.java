package com.sadoon.cbotback.api;

import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;

import java.util.Arrays;

public interface KrakenResponse {

    String[] getErrors();

    default <T extends KrakenResponse> T checkErrors(T response, String[] errors) throws ExchangeRequestException {
        if (Arrays.stream(errors).findFirst().isPresent()) {
            throw new ExchangeRequestException(ExchangeName.KRAKEN, Arrays.toString(errors));
        }
        return response;
    }

    default void checkErrors(String[] errors) throws ExchangeRequestException {
        if (Arrays.stream(errors).findFirst().isPresent()) {
            throw new ExchangeRequestException(ExchangeName.KRAKEN, Arrays.toString(errors));
        }
    }

    void setErrors(String[] errors);
}
