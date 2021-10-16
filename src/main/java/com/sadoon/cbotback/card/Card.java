package com.sadoon.cbotback.card;

import java.math.BigDecimal;
import java.util.Map;

public class Card {
    private String cardName;

    private Map<String, BigDecimal> balances;

    public Card() {
    }

    public Card(String cardName, Map<String, BigDecimal> balances) {
        this.cardName = cardName;
        this.balances = balances;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, BigDecimal> balances) {
        this.balances = balances;
    }
}