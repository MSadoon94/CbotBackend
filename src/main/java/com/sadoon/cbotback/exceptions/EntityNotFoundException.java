package com.sadoon.cbotback.exceptions;

public class EntityNotFoundException extends CustomException {

    public EntityNotFoundException(String message) {
        super(String.format("%s was not found.", message));
    }

    public EntityNotFoundException(String message, ApiError subError) {
        super(String.format("%s was not found.", message), subError);
        this.subError = subError;
    }
}
