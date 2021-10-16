package com.sadoon.cbotback.exceptions;

import java.util.Optional;

public abstract class CustomException extends Exception {
    protected transient ApiError subError;

    protected CustomException(String message) {
        super(message);
    }

    protected CustomException(String message, ApiError subError) {
        super(message);
        this.subError = subError;
    }

    public Optional<ApiError> getSubError() {
        return Optional.ofNullable(subError);
    }

    protected void setSubError(ApiError subError) {
        this.subError = subError;
    }
}
