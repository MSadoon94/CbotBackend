package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.executor.EntryScanner;
import com.sadoon.cbotback.executor.PriceCalculator;
import com.sadoon.cbotback.security.credentials.CredentialsService;

public class Exchange {
    private ExchangeName exchangeName;
    private CredentialsService credentialsService;
    private ExchangeWebSocket webSocket;
    private ExchangeMessageFactory messageFactory;
    private ExchangeWebClient webClient;
    private PriceCalculator priceCalculator;
    private EntryScanner entryScanner;
    private ExchangeResponseHandler responseHandler;
    private ExchangeMessageProcessor messageProcessor;
    private AssetTracker tracker;

    public Exchange setPriceCalculator(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
        return this;
    }

    public Exchange setEntryScanner(EntryScanner entryScanner) {
        this.entryScanner = entryScanner;
        return this;
    }

    public ExchangeName getExchangeName() {
        return exchangeName;
    }

    public Exchange setExchangeName(ExchangeName exchangeName) {
        this.exchangeName = exchangeName;
        return this;
    }

    public CredentialsService getCredentialsService() {
        return credentialsService;
    }

    public Exchange setCredentialsService(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
        return this;
    }

    public ExchangeWebSocket getWebSocket() {
        return webSocket;
    }

    public Exchange setWebSocket(ExchangeWebSocket webSocket) {
        this.webSocket = webSocket;
        return this;
    }

    public ExchangeMessageFactory getMessageFactory() {
        return messageFactory;
    }

    public Exchange setMessageFactory(ExchangeMessageFactory messageFactory) {
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

    public PriceCalculator getPriceCalculator() {
        return priceCalculator;
    }

    public EntryScanner getEntryScanner() {
        return entryScanner;
    }

    public ExchangeResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public Exchange setResponseHandler(ExchangeResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public ExchangeMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public Exchange setMessageProcessor(ExchangeMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
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