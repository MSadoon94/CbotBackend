package com.sadoon.cbotback.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "exchange")
public class ExchangeProperties {
    private Map<String, String> urls;
    private Map<String, String> websockets;

    public Map<String, String> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public Map<String, String> getWebsockets() {
        return websockets;
    }

    public void setWebsockets(Map<String, String> websockets) {
        this.websockets = websockets;
    }
}
