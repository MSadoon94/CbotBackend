package com.sadoon.cbotback.exceptions.password;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class CardPasswordEncryptionException extends CustomException {

    public CardPasswordEncryptionException(String message, ApiError subError) {
        super(String.format("Error while encrypting password: %s", message), subError);
    }

    public CardPasswordEncryptionException(String message) {
        super(String.format("Error while encrypting password: %s", message));
    }
}
