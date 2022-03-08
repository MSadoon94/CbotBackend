package com.sadoon.cbotback.websocket;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

public class BrokerageWebSocket {

    private WebSocketClient client;
    private URI webSocketURI;
    private Flux<Function<WebSocketSession, Mono<Void>>> sendFunctions = Flux.just();
    private Flux<Function<WebSocketSession, Flux<WebSocketMessage>>> receiveFunctions = Flux.just();

    public BrokerageWebSocket(WebSocketClient client, URI webSocketURI) {
        this.client = client;
        this.webSocketURI = webSocketURI;
        startClient();
    }

    public BrokerageWebSocket addSendFunction(Function<WebSocketSession, Mono<Void>> function) {
        sendFunctions = sendFunctions.concatWithValues(function);
        return this;
    }

    public BrokerageWebSocket addReceiveFunction(Function<WebSocketSession, Flux<WebSocketMessage>> function) {
        receiveFunctions = receiveFunctions.concatWithValues(function);
        return this;
    }

    private void startClient() {
        client.execute(webSocketURI, session -> sendFunctions.flatMap(function -> function.apply(session))
                        .thenMany(
                                receiveFunctions.flatMap(function -> function.apply(session)))
                        .then())
                .onErrorResume(Mono::error)
                .subscribe();
    }
}