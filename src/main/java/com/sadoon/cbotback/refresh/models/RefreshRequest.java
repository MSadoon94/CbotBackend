package com.sadoon.cbotback.refresh.models;

public class RefreshRequest {
    private final String jwt;
    private final String username;

    public RefreshRequest(String jwt, String username) {
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
