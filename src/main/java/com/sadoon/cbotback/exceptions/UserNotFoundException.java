package com.sadoon.cbotback.exceptions;

public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(String username) {
        super(String.format("User with username: '%s'", username));
    }

    public UserNotFoundException(String username, ApiError subError) {
        super(String.format("User with username: '%s'", username));
        this.subError = subError;
    }
}
