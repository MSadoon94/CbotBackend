package com.sadoon.cbotback.user.models;

public class LoginRequest {

    private String username;
    private String password;
    private String userId;

    public LoginRequest(){

    }
    public LoginRequest(String username, String password, String userId) {
        this.username = username;
        this.password = password;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
