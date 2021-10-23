package com.sadoon.cbotback.exceptions;

public class CardPasswordEncryptionException extends CustomException {

    public CardPasswordEncryptionException(String message, ApiError subError) {
        super(String.format("Error while encrypting password: %s", message), subError);
    }

    public CardPasswordEncryptionException(String message) {
        super(String.format("Error while encrypting password: %s", message));
    }
}
