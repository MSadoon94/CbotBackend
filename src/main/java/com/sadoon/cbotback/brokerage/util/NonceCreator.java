package com.sadoon.cbotback.brokerage.util;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NonceCreator {

    private int nonce;

    public String createNonce() {
        nonce++;
        long timestamp = (new Date()).getTime();
        return timestamp + String.format("%04d", nonce);
    }
}
