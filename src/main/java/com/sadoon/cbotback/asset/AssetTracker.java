package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.PayloadType;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageProcessor;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
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
    private UserService userService;
    private ExchangeMessageFactory messageFactory;
    private ExchangeMessageProcessor messageProcessor;
    private List<String> pairs = new ArrayList<>();

    public AssetTracker(UserService userService,
                        ExchangeMessageFactory messageFactory,
                        ExchangeMessageProcessor messageProcessor) {
        this.userService = userService;
        this.messageFactory = messageFactory;
        this.messageProcessor = messageProcessor;
    }

    public Function<Flux<Trade>, Flux<Trade>> addCurrentPrice(User user) {
        return tradeFeedIn -> tradeFeedIn
                .doOnNext(trade -> {
                    addPairs(trade);
                    subscribeTradeToPriceChanges(trade, user);
                })
                .flatMap(trade -> tickerMessageFeed(getUpdateParams(trade)))
                .zipWith(tradeFeedIn)
                .map(tuple -> tuple.getT2()
                        .setCurrentPrice(new BigDecimal(tuple.getT1())));
    }

    private void subscribeTradeToPriceChanges(Trade trade, User user) {
        tickerMessageFeed(getUpdateParams(trade))
                .subscribe(price -> userService.updateTrade(user, trade.setCurrentPrice(new BigDecimal(price))));
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

        return messageProcessor.sendMessage(messageFactory.tickerSubscribe(pairs))
                .thenMany(
                        messageProcessor.convertAndSendUpdates(toPayload(type), destination));
    }

    private Function<Flux<String>, Flux<String>> toPayload(StrategyType type) {
        return toTickerMessage.andThen(messageFeed ->
                messageFeed.map(message -> message.getPrice(type)));
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