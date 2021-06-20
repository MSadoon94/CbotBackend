package com.sadoon.cbotback.home.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class BrokerageCard {
    private final String account;
    private final String password;

    public BrokerageCard(String account, String password) {
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
