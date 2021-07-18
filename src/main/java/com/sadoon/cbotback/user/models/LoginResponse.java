package com.sadoon.cbotback.user.models;

import org.springframework.http.HttpHeaders;

import java.util.Date;

public class LoginResponse {
    private final String jwt;
    private final Date expiration;
    private HttpHeaders header;

    public LoginResponse(String jwt, Date expiration) {
        this.jwt = jwt;
        this.expiration = expiration;
    }

    public String getJwt() {
        return jwt;
    }

    public Date getExpiration() {
        return expiration;
    }

    public HttpHeaders getHeader() {
        return header;
    }

    public void setHeader(HttpHeaders header) {
        this.header = header;
    }
}
