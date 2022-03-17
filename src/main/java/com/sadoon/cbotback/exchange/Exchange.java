package com.sadoon.cbotback.exchange;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.security.SecurityCredentials;

public class Exchange {
    private ExchangeType name;
    private SecurityCredentials credentials;
    private ExchangeWebSocket webSocket;
    private BrokerageMessageFactory messageFactory;
    private ExchangeWebClient webClient;
    private ExchangeResponseHandler responseHandler;
    private AssetTracker tracker;

    public ExchangeType getName() {
        return name;
    }

    public Exchange setName(ExchangeType name) {
        this.name = name;
        return this;
    }

    public ExchangeWebSocket getWebSocket() {
        return webSocket;
    }

    public Exchange setWebSocket(ExchangeWebSocket webSocket) {
        this.webSocket = webSocket;
        return this;
    }

    public BrokerageMessageFactory getMessageFactory() {
        return messageFactory;
    }

    public Exchange setMessageFactory(BrokerageMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
        return this;
    }

    public ExchangeWebClient getWebClient() {
        return webClient;
    }

    public Exchange setWebClient(ExchangeWebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    public SecurityCredentials getCredentials() {
        return credentials;
    }

    public Exchange setCredentials(SecurityCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public ExchangeResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public Exchange setResponseHandler(ExchangeResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public AssetTracker getTracker() {
        return tracker;
    }

    public Exchange setTracker(AssetTracker tracker) {
        this.tracker = tracker;

        return this;
    }
}