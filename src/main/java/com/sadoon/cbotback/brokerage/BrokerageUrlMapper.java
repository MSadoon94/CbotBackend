package com.sadoon.cbotback.brokerage;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrokerageUrlMapper {

    private Map<String, String> brokerages = new LinkedHashMap<>();

    public BrokerageUrlMapper() {
        setBrokerages();
    }

    public String getBrokerageUrl(String brokerage) {
        return brokerages.get(brokerage);
    }

    private void setBrokerages() {
        brokerages.put("kraken", "https://api.kraken.com");
    }
}
