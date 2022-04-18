package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.PayloadType;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageProcessor;
import com.sadoon.cbotback.strategy.StrategyType;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class AssetTracker {
    private ExchangeMessageFactory messageFactory;
    private ExchangeMessageProcessor messageHandler;
    private List<String> pairs = new ArrayList<>();

    public AssetTracker(ExchangeMessageFactory messageFactory,
                        ExchangeMessageProcessor messageProcessor) {
        this.messageFactory = messageFactory;
        this.messageHandler = messageProcessor;
    }

    public Flux<Trade> getTrades(Flux<Trade> tradeFeedIn) {
        return tradeFeedIn
                .doOnNext(this::addPairs)
                .map(this::getUpdateParams)
                .flatMap(this::tickerMessageFeed)
                .zipWith(tradeFeedIn)
                .map(tuple -> tuple.getT2()
                        .setCurrentPrice(new BigDecimal(tuple.getT1())));
    }

    private Map<String, String> getUpdateParams(Trade trade) {
        return Map.of(
                "Pair", trade.getPair(),
                "Exchange", trade.getExchange().name().toLowerCase(),
                "StrategyType", trade.getType().name()
        );
    }

    private void addPairs(Trade trade) {
        if (!pairs.contains(trade.getPair())) {
            pairs.add(trade.getPair());
        }
    }

    private Flux<String> tickerMessageFeed(Map<String, String> updateParams) {
        StrategyType type = StrategyType.valueOf(updateParams.get("StrategyType"));
        String destination =
                String.format("/topic/price/%1s/%2s",
                        updateParams.get("Exchange").toLowerCase(),
                        updateParams.get("Pair").toLowerCase().replace("/", ""));

        return messageHandler.sendMessage(messageFactory.tickerSubscribe(pairs))
                .thenMany(
                        messageHandler.convertAndSendUpdates(toPayload(type), destination));
    }

    private Function<Flux<String>, Flux<String>> toPayload(StrategyType type) {
        return toTickerMessage.andThen(messageFeed ->
                messageFeed.map(message -> message.getPrice(type)));
    }

    private Function<Flux<String>, Flux<TickerMessage>> toTickerMessage = (messageFeed) ->
            messageFeed
                    .filter(messageFilter())
                    .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release)
                    .flatMap(message -> messageFactory.<TickerMessage>tickerMessage(message));

    private Predicate<String> messageFilter() {
        return message ->
                !PayloadType.getType(message).equals(PayloadType.EVENT) &&
                        PayloadType.getType(message).equals(PayloadType.TICKER);
    }
}