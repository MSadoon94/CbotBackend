package com.sadoon.cbotback.refresh.models;

import java.util.Date;

public class RefreshResponse {
    private String jwt;
    private Date jwtExpiration;
    private String tokenType = "Bearer";

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
}
