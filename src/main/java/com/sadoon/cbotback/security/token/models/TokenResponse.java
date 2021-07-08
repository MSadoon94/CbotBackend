package com.sadoon.cbotback.security.token.models;

import org.springframework.http.HttpHeaders;

public class TokenResponse {
    private String jwt;
    private String refreshToken;
    private String tokenType = "Bearer";
    private HttpHeaders headers;

    public TokenResponse(String jwt, String refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }

    public String getJwt() {
        return jwt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
