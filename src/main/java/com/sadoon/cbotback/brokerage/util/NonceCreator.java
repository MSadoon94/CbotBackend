package com.sadoon.cbotback.brokerage.util;

import java.util.Date;

public class NonceCreator {

    public String createNonce(int count) {
        long timestamp = (new Date()).getTime();
        return timestamp + String.format("%04d", count);
    }
}
