package com.sadoon.cbotback.card.models;

import com.sadoon.cbotback.brokerage.model.BrokerageApiRequest;

public class CardApiRequest implements BrokerageApiRequest {
    private String cardName;
    private String account;
    private String password;
    private String brokerage;

    public CardApiRequest() {
    }

    public CardApiRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExchange() {
        return brokerage;
    }

    public void setBrokerage(String brokerage) {
        this.brokerage = brokerage;
    }

}
