package com.sadoon.cbotback.card.models;

import com.sadoon.cbotback.brokerage.model.Balances;

public class Card {
    private String cardName, password;
    private Balances balances;

    public Card() {
    }

    public Card(String cardName, String password, Balances balances) {
        this.cardName = cardName;
        this.password = password;
        this.balances = balances;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Balances getBalances() {
        return balances;
    }

    public void setBalances(Balances balances) {
        this.balances = balances;
    }
}