package com.sadoon.cbotback.card.models;

public class CardPasswordVerificationRequest {
    private String cardName;
    private String password;

    public CardPasswordVerificationRequest() {
    }

    public CardPasswordVerificationRequest(String cardName, String password) {
        this.cardName = cardName;
        this.password = password;
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
}
