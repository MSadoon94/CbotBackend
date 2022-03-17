package com.sadoon.cbotback.exchange;

public enum ExchangeType {
    KRAKEN("result");

    private final String responseKey;

    ExchangeType(String responseKey){
        this.responseKey = responseKey;
    }

    public String getResponseKey() {
        return responseKey;
    }
}