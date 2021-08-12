package com.sadoon.cbotback.refresh.models;

import org.springframework.http.HttpHeaders;

import java.util.Date;

public class RefreshResponse {
    private String jwt;
    private Date jwtExpiration;
    private String tokenType = "Bearer";
    private HttpHeaders headers;
    private String message;

    public RefreshResponse(String jwt, Date jwtExpiration) {
        this.jwt = jwt;
        this.jwtExpiration = jwtExpiration;
    }

    public String getJwt() {
        return jwt;
    }

    public Date getJwtExpiration() {
        return jwtExpiration;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
