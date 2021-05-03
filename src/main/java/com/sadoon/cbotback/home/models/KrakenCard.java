package com.sadoon.cbotback.home.models;

import com.sadoon.cbotback.brokerage.kraken.KrakenAccount;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class KrakenCard {
    private final String account;
    private final String password;

    private KrakenAccount krakenAccount;

    public KrakenCard(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public KrakenAccount getKrakenAccount() {
        return krakenAccount;
    }

    public void setKrakenAccount(KrakenAccount krakenAccount) {
        this.krakenAccount = krakenAccount;
    }
}
