package com.sadoon.cbotback.home.models;

public class CardRequest {
    private String account;
    private String password;
    private String balance;

    public CardRequest(String account, String password, String balance) {
        this.account = account;
        this.password = password;
        this.balance = balance;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getBalance() {
        return balance;
    }

}
