package com.sadoon.cbotback.exceptions;

public class RefreshExpiredException extends UnauthorizedUserException{
    public RefreshExpiredException() {
        super("Refresh token has expired.");
    }
}
