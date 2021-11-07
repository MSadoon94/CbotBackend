package com.sadoon.cbotback.exceptions;

public class CardNotFoundException extends EntityNotFoundException {
    public CardNotFoundException(String cardName) {
        super(String.format("Card with name '%s'", cardName));
    }

    public CardNotFoundException(String cardName, ApiError subError) {
        super(String.format("Card with name '%s'", cardName), subError);
    }
}
