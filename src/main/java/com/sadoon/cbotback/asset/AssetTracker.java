package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.websocket.*;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.*;

import java.util.Map;
import java.util.function.Predicate;

public class AssetTracker {

    private Map<String, BrokerageSocketModule> socketModules;
    private BrokerageSocketModule socketService;
    private BrokerageWebSocket client;
    private BrokerageMessageFactory messageFactory;
    private WebSocketFunctions functions;
    private Sinks.Many<Mono<? extends TickerMessage>> tickerFeed = Sinks.many().multicast().onBackpressureBuffer();

    public AssetTracker(Map<String, BrokerageSocketModule> socketModules,
                        String brokerageName) {
        this.socketModules = socketModules;
        this.socketService = socketModules.get(brokerageName);

        unpackModule();

        this.functions = new WebSocketFunctions();
    }

    public Flux<Mono<? extends TickerMessage>> getTickerFlux(AssetPairs assetPairs) {

        client.addSendFunction(
                functions.sendMessage(
                        messageFactory.tickerSubscribe(assetPairs)
                )
        ).addReceiveFunction(
                functions.receiveMessages(tickerSubscriber(), messageFilter())
        );

        return tickerFeed.asFlux();
    }

    private BaseSubscriber<WebSocketMessage> tickerSubscriber() {
        return new BaseSubscriber<>() {

            @Override
            protected void hookOnNext(WebSocketMessage message) {
                requestUnbounded();
                tickerFeed.tryEmitNext(messageFactory.tickerMessage(message.getPayloadAsText()));
            }
        };
    }

    private Predicate<WebSocketMessage> messageFilter() {
        return message ->
                !PayloadType.getType(message).equals(PayloadType.EVENT) &&
                        PayloadType.getType(message).equals(PayloadType.TICKER);
    }

    private void unpackModule() {
        this.client = socketService.getWebSocketClient();
        this.messageFactory = socketService.getMessageFactory();
    }
}