package com.sadoon.cbotback.exceptions;

public class LoginCredentialsException extends CredentialsException {
    public LoginCredentialsException() {
        super("Username or Password");
    }

    public LoginCredentialsException(ApiError subError) {
        super("Username or Password", subError);
    }
}
