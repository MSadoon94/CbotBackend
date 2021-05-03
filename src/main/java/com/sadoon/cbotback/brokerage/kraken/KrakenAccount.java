package com.sadoon.cbotback.brokerage.kraken;

import java.util.Map;

public class KrakenAccount {

    private Map<String, String> accountBalance;

    public Map<String, String> getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Map<String, String> accountBalance) {
        this.accountBalance = accountBalance;
    }
}
