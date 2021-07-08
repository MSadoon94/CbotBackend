package com.sadoon.cbotback.security.token.models;

public class TokenRequest {
    private final String refreshToken;
    private final String username;

    public TokenRequest(String refreshToken, String username) {
        this.refreshToken = refreshToken;
        this.username = username;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getUsername() {
        return username;
    }
}
