package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exceptions.exchange.ExchangeConnectionException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

public class ExchangeMessageProcessor {
    private ExchangeWebSocket socket;
    private ExchangeMessageHandler messageHandler;
    private SimpMessagingTemplate messagingTemplate;

    public ExchangeMessageProcessor(
            ExchangeWebSocket socket,
            ExchangeMessageHandler messageHandler,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.socket = socket;
        this.messageHandler = messageHandler;
        this.messagingTemplate = messagingTemplate;
    }

    public Mono<Void> sendMessage(Mono<String> messageMono) {
        connectToWebSocket();
        return Mono.create(sink -> {
            Sinks.EmitResult result = messageHandler.sendMessage(messageMono);
            if (result.isSuccess()) {
                sink.success();
            } else {
                sink.error(new ExchangeConnectionException(ExchangeName.KRAKEN, result));
            }
        });
    }

    public <T> Flux<T> convertMessages(Function<Flux<String>, Flux<T>> transformation) {
        return messageHandler.getMessageFeed()
                .transform(transformation);
    }

    public <T> Flux<T> convertAndSendUpdates(Function<Flux<String>, Flux<T>> transformation, String destination) {
        Flux<T> feed = messageHandler.getMessageFeed()
                .transform(transformation)
                .doOnNext(payload -> messagingTemplate.convertAndSend(destination, payload))
                .share();
        keepFeedAlive(feed);
        return feed;
    }

    private <T> void keepFeedAlive(Flux<T> feed) {
        feed.subscribe();
    }

    private void connectToWebSocket() {
        if (!socket.isSocketAlive()) {
            socket.execute(messageHandler);
        }
    }
}