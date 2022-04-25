package com.sadoon.cbotback.exchange.structure;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.function.Function;

public class ExchangeWebSocket {
    private boolean isSocketAlive = false;
    private WebSocketClient client;
    private URI webSocketURI;
    private Flux<Function<WebSocketSession, Mono<Void>>> sendFunctions = Flux.just();
    private Flux<Function<WebSocketSession, Flux<WebSocketMessage>>> receiveFunctions = Flux.just();
    private Sinks.Many<WebSocketMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    public ExchangeWebSocket(WebSocketClient client, URI webSocketURI) {
        this.client = client;
        this.webSocketURI = webSocketURI;
    }

    public boolean isSocketAlive() {
        return isSocketAlive;
    }

    public ExchangeWebSocket addSendFunction(Function<WebSocketSession, Mono<Void>> function) {
        sendFunctions = sendFunctions.concatWithValues(function);
        return this;
    }

    public ExchangeWebSocket addReceiveFunction(Function<WebSocketSession, Flux<WebSocketMessage>> function) {
        receiveFunctions = receiveFunctions.concatWithValues(function);
        return this;
    }

    public Flux<WebSocketMessage> getMessageFeed() {
        return messageSink
                .asFlux()
                .share();
    }

    public void execute(WebSocketHandler handler) {
        client.execute(webSocketURI, handler)
                .doFirst(() -> isSocketAlive = true)
                .doOnTerminate(() -> isSocketAlive = false)
                .subscribe();
    }
}