package com.sadoon.cbotback.exceptions;

public class KrakenRequestException extends CustomException {

    public KrakenRequestException(String message) {
        super(String.format("Kraken responded with this list of errors: '%s'.", message));
    }

    public KrakenRequestException(String message, ApiError subError) {
        super(String.format("Kraken responded with this list of errors: '%s'.", message), subError);
        this.subError = subError;
    }
}
