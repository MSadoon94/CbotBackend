package com.sadoon.cbotback.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exchange.kraken.KrakenMessageFactory;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.exp.CredentialManager;
import com.sadoon.cbotback.exchange.kraken.KrakenResponseHandler;
import com.sadoon.cbotback.security.AESKeyUtil;
import com.sadoon.cbotback.security.KeyStoreUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URI;

@Component
public class ExchangeSupplier {

    @Bean
    @Lazy
    ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    @Lazy
    CredentialManager credentialManager(AppProperties props) {
        return new CredentialManager(
                new AESKeyUtil(),
                new SignatureCreator(),
                new KeyStoreUtil(props)
        );
    }

    @Bean(name = "KRAKEN")
    @Lazy
    Exchange krakenExchange(ObjectMapper mapper,
                            CredentialManager credentialManager) {
        ExchangeWebSocket webSocket = new ExchangeWebSocket(
                new ReactorNettyWebSocketClient(),
                URI.create("wss://ws.kraken.com")
        );
        BrokerageMessageFactory messageFactory = new KrakenMessageFactory(mapper);
        return new Exchange()
                .setName(ExchangeNames.KRAKEN)
                .setWebSocket(webSocket)
                .setMessageFactory(messageFactory)
                .setWebClient(new KrakenWebClient(new NonceCreator(), "https://api.kraken.com"))
                .setResponseHandler(new KrakenResponseHandler(mapper))
                .setTracker(new AssetTracker(webSocket, messageFactory));
    }
}