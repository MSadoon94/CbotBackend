package com.sadoon.cbotback.exchange.structure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.exchange.kraken.KrakenMessageFactory;
import com.sadoon.cbotback.exchange.kraken.KrakenResponseHandler;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.security.credentials.CredentialsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Lazy
public class ExchangeSupplier {

    private Map<ExchangeName, Exchange> exchangeRegistry = new LinkedHashMap<>();

    @Bean
    ObjectMapper mapper() {
        return new ObjectMapper();
    }

    public Exchange getExchange(ExchangeName type) {
        return exchangeRegistry.get(type);
    }

    public Map<ExchangeName, Exchange> getExchangeRegistry() {
        return exchangeRegistry;
    }

    @Bean
    Exchange krakenExchange(ObjectMapper mapper, CredentialsService credentialsService) {
        KrakenResponseHandler responseHandler = new KrakenResponseHandler(mapper);
        ExchangeWebClient webClient
                = new KrakenWebClient(responseHandler, new NonceCreator(), "https://api.kraken.com");
        ExchangeWebSocket webSocket = new ExchangeWebSocket(new ReactorNettyWebSocketClient(), URI.create("wss://ws.kraken.com"));
        WebSocketMessageHandler messageHandler = new WebSocketMessageHandler();
        KrakenMessageFactory messageFactory = new KrakenMessageFactory(mapper);

        Exchange kraken = new Exchange()
                .setExchangeName(ExchangeName.KRAKEN)
                .setCredentialsService(credentialsService)
                .setWebSocket(webSocket)
                .setMessageFactory(messageFactory)
                .setTracker(new AssetTracker(webSocket, messageFactory, messageHandler))
                .setWebClient(webClient)
                .setResponseHandler(responseHandler);

        exchangeRegistry.put(ExchangeName.KRAKEN, kraken);
        kraken.getUserTradeFeed()
                .subscribe();
        return kraken;
    }
}