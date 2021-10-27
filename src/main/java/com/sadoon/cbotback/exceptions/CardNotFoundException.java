package com.sadoon.cbotback.exceptions;

public class CardNotFoundException extends CustomException {
    public CardNotFoundException(String cardName) {
        super(String.format("Card with name '%s' could not be found.", cardName));
    }

    public CardNotFoundException(String cardName, ApiError subError) {
        super(String.format("Card with name '%s' could not be found.", cardName), subError);
    }
}
