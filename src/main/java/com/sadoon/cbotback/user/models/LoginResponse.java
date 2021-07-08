package com.sadoon.cbotback.user.models;

import com.sadoon.cbotback.security.token.models.RefreshToken;

public class LoginResponse {
    private final String jwt;
    private final RefreshToken refreshToken;

    public LoginResponse(String jwt, RefreshToken refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }


    public String getJwt() {
        return jwt;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }
}
