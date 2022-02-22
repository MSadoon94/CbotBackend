package com.sadoon.cbotback.exceptions.password;

import com.sadoon.cbotback.exceptions.ApiError;

public class LoginCredentialsException extends CredentialsException {
    public LoginCredentialsException() {
        super("Username or Password");
    }

    public LoginCredentialsException(ApiError subError) {
        super("Username or Password", subError);
    }
}
