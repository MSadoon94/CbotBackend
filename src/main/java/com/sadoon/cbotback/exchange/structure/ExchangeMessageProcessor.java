package com.sadoon.cbotback.exchange.structure;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class ExchangeMessageProcessor {
    private ExchangeWebSocket socket;
    private boolean isSocketAlive = false;
    private Mono<Void> socketConnection;

    private ExchangeMessageHandler exchangeMessageHandler;
    private SimpMessagingTemplate messagingTemplate;

    public ExchangeMessageProcessor(
            ExchangeWebSocket socket,
            ExchangeMessageHandler messageHandler,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.socket = socket;
        this.exchangeMessageHandler = messageHandler;
        this.messagingTemplate = messagingTemplate;
    }

    public Mono<Void> sendMessage(Mono<String> messageMono) {
        connectToWebSocket();
        return exchangeMessageHandler.sendMessage(messageMono);
    }

    public <T> Flux<T> convertMessages(Function<Flux<String>, Flux<T>> transformation) {
        return exchangeMessageHandler.getMessageFeed()
                .transform(transformation);
    }

    public <T> Flux<T> convertAndSendUpdates(Function<Flux<String>, Flux<T>> transformation, String destination) {
        Flux<T> feed = exchangeMessageHandler.getMessageFeed()
                .transform(transformation)
                .doOnNext(payload -> messagingTemplate.convertAndSend(destination, payload))
                .share();
        keepFeedAlive(feed);
        return feed;
    }

    private <T> void keepFeedAlive(Flux<T> feed){
        feed.subscribe();
    }

    private void connectToWebSocket() {
        if (!isSocketAlive) {
            socketConnection = socket.execute(exchangeMessageHandler)
                    .doOnTerminate(() -> isSocketAlive = false);
        }
        socketConnection.subscribe();
    }
}