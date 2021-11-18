package com.sadoon.cbotback.user.models;

import java.util.Date;

public class LoginResponse {
    private final String username;
    private final Date expiration;
    private boolean isLoggedIn = false;

    public LoginResponse(String username, Date expiration) {
        this.username = username;
        this.expiration = expiration;
    }

    public String getUsername() {
        return username;
    }

    public Date getExpiration() {
        return expiration;
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

}
