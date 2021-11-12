package com.sadoon.cbotback.exceptions;

public class RefreshTokenNotFoundException extends EntityNotFoundException{
    public RefreshTokenNotFoundException(String token) {
        super(String.format("Refresh Token: %s", token));
    }

    public RefreshTokenNotFoundException(String token, ApiError subError) {
        super(String.format("Refresh Token: %s", token), subError);
    }
}
