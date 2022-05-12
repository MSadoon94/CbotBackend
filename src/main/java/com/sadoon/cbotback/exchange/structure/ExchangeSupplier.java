package com.sadoon.cbotback.exchange.structure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.kraken.KrakenMessageFactory;
import com.sadoon.cbotback.exchange.kraken.KrakenResponseHandler;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.executor.EntryScanner;
import com.sadoon.cbotback.executor.PriceCalculator;
import com.sadoon.cbotback.security.credentials.CredentialsService;
import com.sadoon.cbotback.security.util.NonceCreator;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.util.ExchangeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ExchangeSupplier {
    private ExchangeProperties exchangeProperties;

    private Map<ExchangeName, ExchangeUtil> exchangeRegistry = new LinkedHashMap<>();

    public ExchangeSupplier(ExchangeProperties exchangeProperties) {
        this.exchangeProperties = exchangeProperties;
    }

    @Bean
    ObjectMapper mapper() {
        return new ObjectMapper();
    }

    public ExchangeUtil getExchange(ExchangeName type) {
        return exchangeRegistry.get(type);
    }

    public Map<ExchangeName, ExchangeUtil> getExchangeRegistry() {
        return exchangeRegistry;
    }

    @Bean
    ExchangeUtil krakenExchange(
            ObjectMapper mapper,
            CredentialsService credentialsService,
            UserService userService,
            SimpMessagingTemplate simpMessagingTemplate) {
        KrakenResponseHandler responseHandler = new KrakenResponseHandler(mapper);
        ExchangeWebClient webClient = new KrakenWebClient(
                responseHandler,
                new NonceCreator(),
                exchangeProperties.getUrls().get(ExchangeName.KRAKEN.name().toLowerCase()));
        ExchangeMessageProcessor messageProcessor =
                new ExchangeMessageProcessor(
                        new ReactorNettyWebSocketClient(),
                        URI.create(exchangeProperties.getWebsockets().get(ExchangeName.KRAKEN.name().toLowerCase())),
                        new ExchangeMessageHandler(),
                        simpMessagingTemplate);
        KrakenMessageFactory messageFactory = new KrakenMessageFactory(mapper);
        ExchangeUtil kraken = new ExchangeUtil()
                .setExchangeName(ExchangeName.KRAKEN)
                .setCredentialsService(credentialsService)
                .setMessageFactory(messageFactory)
                .setMessageProcessor(messageProcessor)
                .setTracker(new AssetTracker(userService, messageFactory, messageProcessor))
                .setWebClient(webClient)
                .setResponseHandler(responseHandler)
                .setEntryScanner(new EntryScanner())
                .setPriceCalculator(new PriceCalculator());

        exchangeRegistry.put(ExchangeName.KRAKEN, kraken);

        return kraken;
    }
}