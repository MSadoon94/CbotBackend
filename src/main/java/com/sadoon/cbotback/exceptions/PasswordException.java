package com.sadoon.cbotback.exceptions;

public class PasswordException extends UnauthorizedUserException {
    public PasswordException(String passwordType) {
        super(String.format("Password for '%s' was incorrect.", passwordType));
    }

    public PasswordException(String passwordType, ApiError subError) {
        super(String.format("Password for '%s' was incorrect.", passwordType), subError);
    }
}
