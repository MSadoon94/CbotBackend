package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.PayloadType;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeWebSocket;
import com.sadoon.cbotback.exchange.structure.WebSocketMessageHandler;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AssetTracker {
    private ExchangeWebSocket socket;
    private ExchangeMessageFactory messageFactory;
    private WebSocketMessageHandler messageHandler;

    private List<String> pairs = new ArrayList<>();

    public AssetTracker(ExchangeWebSocket socket,
                        ExchangeMessageFactory messageFactory,
                        WebSocketMessageHandler messageHandler) {
        this.socket = socket;
        this.messageFactory = messageFactory;
        this.messageHandler = messageHandler;
        socket.execute(messageHandler);
    }

    public Flux<Trade> getTrades(Flux<Trade> tradeFeedIn) {
        socket.execute(messageHandler);

        return tradeFeedIn.doOnNext(trade -> {
                    if (!pairs.contains(trade.getPair())) {
                        pairs.add(trade.getPair());
                    }
                })
                .zipWith(messageHandler
                        .sendMessage(messageFactory.tickerSubscribe(pairs))
                        .thenMany(messageHandler.getMessageFeed())
                        .filter(messageFilter())
                        .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release)
                        .flatMap(message -> messageFactory.tickerMessage(message))
                ).map(tuple -> tuple.getT1()
                        .setCurrentPrice(new BigDecimal(tuple.getT2().getPrice(tuple.getT1().getType()))));
    }

    private Predicate<String> messageFilter() {
        return message ->
                !PayloadType.getType(message).equals(PayloadType.EVENT) &&
                        PayloadType.getType(message).equals(PayloadType.TICKER);
    }
}