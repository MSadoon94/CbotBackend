package com.sadoon.cbotback.home.models;

public class CardRequest {
    private final String account;
    private final String password;

    public CardRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }
}
