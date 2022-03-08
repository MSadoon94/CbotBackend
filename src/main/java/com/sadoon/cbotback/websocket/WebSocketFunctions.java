package com.sadoon.cbotback.websocket;

import org.reactivestreams.Subscriber;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

public class WebSocketFunctions {

    public Function<WebSocketSession, Mono<Void>> sendMessage(Mono<String> message) {
        return session -> session.send(message.map(session::textMessage));
    }

    public Function<WebSocketSession, Flux<WebSocketMessage>> receiveMessages( Subscriber<WebSocketMessage> subscriber,
                                                                               Predicate<WebSocketMessage> messageFilter
                                                                             ) {
        return session -> {
            Flux<WebSocketMessage> flux = session.receive()
                    .filter(messageFilter)
                    .share();
            flux.subscribe(subscriber);
            return flux;
        };
    }

}