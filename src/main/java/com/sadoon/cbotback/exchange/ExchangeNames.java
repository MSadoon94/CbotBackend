package com.sadoon.cbotback.exchange;

public enum ExchangeNames {
    KRAKEN("result");

    private final String responseKey;

    ExchangeNames(String responseKey){
        this.responseKey = responseKey;
    }

    public String getResponseKey() {
        return responseKey;
    }
}