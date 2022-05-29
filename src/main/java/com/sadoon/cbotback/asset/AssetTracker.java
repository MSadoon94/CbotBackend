package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.PayloadType;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageProcessor;
import com.sadoon.cbotback.executor.CandleProcessor;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AssetTracker {
    private UserService userService;
    private ExchangeMessageFactory messageFactory;
    private ExchangeMessageProcessor messageProcessor;
    private CandleProcessor candleProcessor;
    private List<String> pairs = new ArrayList<>();

    public AssetTracker(UserService userService,
                        ExchangeMessageFactory messageFactory,
                        ExchangeMessageProcessor messageProcessor,
                        CandleProcessor candleProcessor
    ) {
        this.userService = userService;
        this.messageFactory = messageFactory;
        this.messageProcessor = messageProcessor;
        this.candleProcessor = candleProcessor;
    }

    public Function<Flux<Trade>, Flux<Trade>> addCurrentPrice(User user) {
        return tradeFeedIn -> tradeFeedIn
                .doOnNext(this::addPairs)
                .flatMap(trade -> {
                    Flux<TickerMessage> tickerFeed = tickerMessageFeed();
                    subscribeTradeToPriceChanges(tickerFeed, user, trade);
                    subscribeTradeToCandleBroadcasts(tickerFeed, trade);
                    return tickerFeed
                            .transform(toPricePayload(trade.getType()));
                })
                .zipWith(tradeFeedIn)
                .map(tuple -> tuple.getT2()
                        .setCurrentPrice(new BigDecimal(tuple.getT1())));
    }

    private void subscribeTradeToPriceChanges(Flux<TickerMessage> tickerFeed, User user, Trade trade) {
        String priceDestination =
                String.format("/topic/price/%1s/%2s",
                        trade.getExchange().name().toLowerCase(),
                        trade.getPair().toLowerCase().replace("/", ""));

        tickerFeed
                .takeWhile(message -> userService.doesUserExist(user))
                .transform(toPricePayload(trade.getType()))
                .doOnNext(payload -> messageProcessor.broadcastMessage(priceDestination, payload))
                .subscribe(price -> userService.updateTrade(user, trade.setCurrentPrice(new BigDecimal(price))));
    }

    private void subscribeTradeToCandleBroadcasts(Flux<TickerMessage> tickerFeed, Trade trade) {
        tickerFeed.transform(candleProcessor.toCandles(trade.getType(), Duration.of(
                        Long.parseLong(trade.getTimeFrame()),
                        ChronoUnit.valueOf(trade.getTimeUnits().toUpperCase()))))
                .subscribe(candle -> messageProcessor.broadcastMessage(
                        String.format("/topic/metrics/candle/%s",
                                trade.getStrategyName()), candle));
    }

    private void addPairs(Trade trade) {
        if (!pairs.contains(trade.getPair())) {
            pairs.add(trade.getPair());
        }
    }

    private Flux<TickerMessage> tickerMessageFeed() {
        return messageProcessor.sendMessage(messageFactory.tickerSubscribe(pairs))
                .thenMany(messageProcessor.convertMessages(toTickerMessage));
    }

    private Function<Flux<TickerMessage>, Flux<String>> toPricePayload(StrategyType type) {
        return messageFeed -> messageFeed.map(message -> message.getPrice(type));
    }

    private Function<Flux<String>, Flux<TickerMessage>> toTickerMessage = messageFeed ->
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