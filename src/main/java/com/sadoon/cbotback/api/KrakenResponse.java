package com.sadoon.cbotback.api;

import com.sadoon.cbotback.exceptions.KrakenRequestException;

import java.util.Arrays;
import java.util.List;

public interface KrakenResponse {

    List<String> getErrors();

    default void checkErrors(List<String> errors) throws KrakenRequestException {
        if (!errors.isEmpty()) {
            throw new KrakenRequestException(Arrays.toString(errors.toArray()));
        }
    }
}
