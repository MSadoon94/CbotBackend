package com.sadoon.cbotback.card.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.sadoon.cbotback.brokerage.model.Balances;

import java.math.BigDecimal;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonProperty.Access;

public class Card {
    private String cardName;
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    @JsonUnwrapped
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

    public Map<String, BigDecimal> getBalances() {
        return balances.getBalances();
    }

    public void setBalances(Balances balances) {
        this.balances = balances;
    }
}