package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.PayloadType;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.structure.BrokerageMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeWebSocket;
import com.sadoon.cbotback.exchange.structure.WebSocketFunctions;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

public class AssetTracker {
    private ExchangeWebSocket socket;
    private BrokerageMessageFactory messageFactory;
    private WebSocketFunctions functions;
    private Flux<Trade> tradeFeedIn;
    private Sinks.Many<Mono<? extends Trade>> tradeFeedOut = Sinks.many().multicast().onBackpressureBuffer();

    public AssetTracker(Flux<Trade> tradeFeedIn,
                        ExchangeWebSocket socket,
                        BrokerageMessageFactory messageFactory) {
        this.tradeFeedIn = tradeFeedIn;
        this.socket = socket;
        this.messageFactory = messageFactory;
        this.functions = new WebSocketFunctions();
        processTrades();
    }

    public Flux<Trade> getTrades() {
        return tradeFeedOut
                .asFlux()
                .flatMap(Mono::flux);
    }

    public void processTrades() {
        tradeFeedIn
                .subscribe(trade ->
                        socket.addSendFunction(functions.sendMessage(messageFactory.tickerSubscribe(List.of(trade.getPair()))))
                                .addReceiveFunction(functions.receiveMessages(tickerSubscriber(trade), messageFilter())));
    }


    private BaseSubscriber<WebSocketMessage> tickerSubscriber(Trade trade) {
        return new BaseSubscriber<>() {
            @Override
            protected void hookOnNext(WebSocketMessage message) {
                requestUnbounded();

                tradeFeedOut.tryEmitNext(
                        messageFactory.tickerMessage(message.getPayloadAsText())
                                .map(tickerMessage ->
                                        trade.setCurrentPrice(new BigDecimal(tickerMessage.getPrice(trade.getType())))));
            }
        };
    }

    private Predicate<WebSocketMessage> messageFilter() {
        return message ->
                !PayloadType.getType(message).equals(PayloadType.EVENT) &&
                        PayloadType.getType(message).equals(PayloadType.TICKER);
    }
}