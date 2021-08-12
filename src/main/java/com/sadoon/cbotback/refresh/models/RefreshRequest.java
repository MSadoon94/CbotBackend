package com.sadoon.cbotback.refresh.models;

public class RefreshRequest {
    private final String username;

    public RefreshRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
