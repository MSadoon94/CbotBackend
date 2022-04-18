package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.executor.EntryScanner;
import com.sadoon.cbotback.executor.PriceCalculator;
import com.sadoon.cbotback.security.credentials.CredentialsService;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.user.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Sinks;

public class Exchange {
    private ExchangeName exchangeName;
    private CredentialsService credentialsService;
    private ExchangeWebSocket webSocket;
    private ExchangeMessageFactory messageFactory;
    private ExchangeWebClient webClient;
    private ExchangeResponseHandler responseHandler;
    private ExchangeMessageProcessor messageProcessor;
    private AssetTracker tracker;
    private Sinks.Many<GroupedFlux<String, Flux<Trade>>> userTradeFeeds
            = Sinks.many().multicast().onBackpressureBuffer();

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

    public Exchange addUserTradeFeeds(GroupedFlux<String, Flux<Trade>> tradeFeed) {
        userTradeFeeds.tryEmitNext(tradeFeed);
        return this;
    }

    public Flux<GroupedFlux<String, Flux<Trade>>> getUserTradeFeed() {
        return userTradeFeeds
                .asFlux()
                .share();
    }

    public Flux<Trade> getTradeFeed(User user, SecurityCredential credential) {
        return new EntryScanner().tradeFeed(
                new PriceCalculator().tradeFeed(
                        webClient.tradeVolumeTradeFeed(
                                credential,
                                webClient.assetPairTradeFeed(
                                        tracker.getTrades(user.getTradeFeed()
                                                .map(trade -> trade.setExchange(exchangeName)))))));
    }
}