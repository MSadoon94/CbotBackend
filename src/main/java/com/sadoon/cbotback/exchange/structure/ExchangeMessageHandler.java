package com.sadoon.cbotback.exchange.structure;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public class ExchangeMessageHandler implements WebSocketHandler {
    private Sinks.Many<Mono<String>> outputMessageFeed =
            Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    private Sinks.Many<String> inputMessageFeed =
            Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    private Sinks.Many<SignalType> inputTerminationSignals =
            Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<Sinks.EmitResult> input = session.receive()
                .map(message -> {
                    String payload = message.retain().getPayloadAsText();
                    message.release();
                    return inputMessageFeed.tryEmitNext(payload);
                })
                .doFinally(inputTerminationSignals::tryEmitNext)
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);

        Flux<Object> output = outputMessageFeed
                .asFlux()
                .flatMap(message -> session.send(message
                        .map(session::textMessage)));
        return Mono.when(output, input);
    }

    public Sinks.EmitResult sendMessage(Mono<String> messageMono) {
        return outputMessageFeed.tryEmitNext(messageMono);
    }

    public Flux<String> getMessageFeed() {
        return inputMessageFeed
                .asFlux()
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }

    public Flux<SignalType> getInputTerminationSignals() {
        return inputTerminationSignals
                .asFlux();
    }
}
