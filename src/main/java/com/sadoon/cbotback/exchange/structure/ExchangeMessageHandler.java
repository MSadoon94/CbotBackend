package com.sadoon.cbotback.exchange.structure;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class ExchangeMessageHandler implements WebSocketHandler {
    private Sinks.Many<String> inputMessageFeed = Sinks.many().replay().all();
    private WebSocketSession session;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<Sinks.EmitResult> input = session.receive()
                .map(message -> {
                    String payload = message.retain().getPayloadAsText();
                    message.release();
                    return inputMessageFeed.tryEmitNext(payload);
                })
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);

        this.session = session;

        return input.then();

    }

    public Mono<Void> sendMessage(Mono<String> messageMono) {
        return session.send(messageMono
                .map(session::textMessage)
        );
    }

    public Flux<String> getMessageFeed() {
        return inputMessageFeed
                .asFlux()
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }
}
