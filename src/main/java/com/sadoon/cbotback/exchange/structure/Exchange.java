package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.executor.EntryScanner;
import com.sadoon.cbotback.executor.PriceCalculator;
import com.sadoon.cbotback.security.credentials.CredentialsService;
import com.sadoon.cbotback.user.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Sinks;

public class Exchange {
    private ExchangeType exchangeName;
    private CredentialsService credentialsService;
    private ExchangeWebSocket webSocket;
    private BrokerageMessageFactory messageFactory;
    private ExchangeWebClient webClient;
    private ExchangeResponseHandler responseHandler;
    private AssetTracker tracker;
    private Sinks.Many<GroupedFlux<String, Flux<Trade>>> userTradeFeeds
            = Sinks.many().multicast().onBackpressureBuffer();

    public ExchangeType getExchangeName() {
        return exchangeName;
    }

    public Exchange setExchangeName(ExchangeType exchangeName) {
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

    public Exchange addUserTradeFeeds(GroupedFlux<String, Flux<Trade>> tradeFeed) {
        userTradeFeeds.tryEmitNext(tradeFeed);
        return this;
    }

    public Flux<GroupedFlux<String, Flux<Trade>>> getUserTradeFeed() {
        return userTradeFeeds
                .asFlux()
                .share();
    }

    public Flux<Trade> getTradeFeed(User user) throws Exception {
        return new EntryScanner().tradeFeed(
                new PriceCalculator().tradeFeed(
                        webClient.tradeVolumeTradeFeed(
                                credentialsService.getCredentials(user.getUsername(), exchangeName.name()),
                                webClient.assetPairTradeFeed(
                                        new AssetTracker(user.getTradeFeed(), webSocket, messageFactory)
                                                .getTrades()))));
    }
}