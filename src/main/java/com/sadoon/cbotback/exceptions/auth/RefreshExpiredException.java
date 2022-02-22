package com.sadoon.cbotback.exceptions.auth;

public class RefreshExpiredException extends UnauthorizedUserException{
    public RefreshExpiredException() {
        super("Refresh token has expired.");
    }
}
