package com.sadoon.cbotback.exceptions;

public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(String username) {
        super(String.format("User with username: '%s' was not found.", username));
    }

    public UserNotFoundException(String username, ApiError subError) {
        super(String.format("User with username: '%s' was not found.", username));
        this.subError = subError;
    }
}
