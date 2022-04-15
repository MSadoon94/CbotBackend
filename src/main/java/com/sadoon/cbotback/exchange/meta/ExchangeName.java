package com.sadoon.cbotback.exchange.meta;

public enum ExchangeName {
    KRAKEN("result");

    private final String responseKey;

    ExchangeName(String responseKey){
        this.responseKey = responseKey;
    }

    public String getResponseKey() {
        return responseKey;
    }
}