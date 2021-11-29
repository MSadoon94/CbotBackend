package com.sadoon.cbotback.user.models;

public class SignUpRequest {

    private String username;

    private String password;

    private String authority;

    public SignUpRequest() {
    }

    public SignUpRequest(String username, String password, String authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
