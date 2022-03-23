package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.*;
import com.sadoon.cbotback.exchange.model.PayloadType;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AssetTracker {
    private ExchangeWebSocket client;
    private BrokerageMessageFactory messageFactory;
    private WebSocketFunctions functions;
    private List<String> pairs = new ArrayList<>();
    private Sinks.Many<Mono<? extends TickerMessage>> tickerFeed = Sinks.many().multicast().onBackpressureBuffer();

    public AssetTracker(ExchangeWebSocket client,
                        BrokerageMessageFactory messageFactory) {
        this.client = client;
        this.messageFactory = messageFactory;

        this.functions = new WebSocketFunctions();
    }

    public Flux<TickerMessage> getTickerFeed(){
        return tickerFeed
                .asFlux()
                .flatMap(Mono::flux);
    }

    public AssetTracker trackPair(String pair) {

        if (!pairs.contains(pair)) {
            pairs.add(pair);
            client.addSendFunction(
                    functions.sendMessage(
                            messageFactory.tickerSubscribe(pairs)
                    )
            ).addReceiveFunction(
                    functions.receiveMessages(tickerSubscriber(), messageFilter())
            );
        }
        return this;
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
}