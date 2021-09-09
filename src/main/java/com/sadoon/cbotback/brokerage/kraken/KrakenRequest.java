package com.sadoon.cbotback.brokerage.kraken;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KrakenRequest {
    private String account;
    private String password;
    private String endpoint;
    private String nonce;
    private MultiValueMap<String, String> bodyValues;

    public KrakenRequest() {
        bodyValues = new LinkedMultiValueMap<>();
    }

    public KrakenRequest(String account, String password) {
        bodyValues = new LinkedMultiValueMap<>();
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
        bodyValues.set("nonce", nonce);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getNonce() {
        return nonce;
    }

    public void addBodyValue(String key, String value) {
        bodyValues.add(key, value);
    }

    public MultiValueMap<String, String> getBodyValues() {
        return bodyValues;
    }

}
