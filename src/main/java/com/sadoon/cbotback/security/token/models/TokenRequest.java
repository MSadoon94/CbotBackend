package com.sadoon.cbotback.security.token.models;

public class TokenRequest {
    private final String jwt;
    private final String username;

    public TokenRequest(String jwt, String username) {
        this.jwt = jwt;
        this.username = username;
    }

    public String getJwt() {
        return jwt;
    }

    public String getUsername() {
        return username;
    }
}
