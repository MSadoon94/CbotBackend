package com.sadoon.cbotback.exchange.structure;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class ExchangeMessageHandler implements WebSocketHandler {
    private Sinks.Many<Mono<String>> outputMessageFeed = Sinks.many().multicast().onBackpressureBuffer();
    private Sinks.Many<String> inputMessageFeed = Sinks.many().replay().all();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> input = session.receive()
                .map(message -> {
                    String payload = message.retain().getPayloadAsText();
                    message.release();
                    return inputMessageFeed.tryEmitNext(payload);
                })
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release)
                .then();

        Mono<Void> output = outputMessageFeed
                .asFlux()
                .flatMap(message -> session.send(message
                        .map(session::textMessage)))
                .then();

        return Mono.zip(output, input).then();
    }

    public Sinks.EmitResult sendMessage(Mono<String> messageMono) {
        return outputMessageFeed.tryEmitNext(messageMono);
    }

    public Flux<String> getMessageFeed() {
        return inputMessageFeed
                .asFlux()
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }
}
