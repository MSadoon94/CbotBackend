package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exceptions.exchange.ExchangeConnectionException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.function.Function;

public class ExchangeMessageProcessor {
    private WebSocketClient client;
    private URI endpointURI;
    private ExchangeMessageHandler messageHandler;
    private SimpMessagingTemplate messagingTemplate;

    public ExchangeMessageProcessor(WebSocketClient client,
                                    URI endpointURI,
                                    ExchangeMessageHandler messageHandler,
                                    SimpMessagingTemplate messagingTemplate) {
        this.client = client;
        this.endpointURI = endpointURI;
        this.messageHandler = messageHandler;
        this.messagingTemplate = messagingTemplate;
        execute();
        monitorInputTermination();
    }

    public Mono<Void> sendMessage(Mono<String> messageMono) {
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

    private void execute() {
        client.execute(endpointURI, messageHandler)
                .subscribe();
    }

    private void monitorInputTermination() {
        messageHandler.getInputTerminationSignals()
                .filter(signal -> signal.equals(SignalType.ON_COMPLETE))
                .subscribe(signal -> execute());
    }
}