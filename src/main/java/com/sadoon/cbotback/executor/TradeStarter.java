package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exp.Trade;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.security.Principal;

public class TradeStarter {
    private UserService userService;
    private PriceCalculator priceCalculator;
    private Exchange exchange;
    private FeeProcessor feeProcessor;
    private Flux<CbotStatus> cbotStatusFlux;
    private Sinks.Many<Trade> tradeFeed = Sinks.many().multicast().onBackpressureBuffer();

    public TradeStarter(UserService userService,
                        PriceCalculator priceCalculator,
                        Exchange exchange,
                        FeeProcessor feeProcessor
    ) {
        this.userService = userService;
        this.priceCalculator = priceCalculator;
        this.feeProcessor = feeProcessor;
        this.exchange = exchange;
        cbotStatusFlux = userService.cbotStatusFlux();
    }

    public Flux<Trade> getTradeFeed() {
        return tradeFeed.asFlux();
    }

    public TradeStarter processTrades(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());

        cbotStatusFlux
                .doOnEach(System.out::println)
                .filter(CbotStatus::isActive)
                .flatMap(status ->
                        Flux.fromStream(
                                user.getStrategies()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> status.activeStrategies().contains(entry.getValue().getName()))
                        )
                )
                .flatMap(entry -> getTradeFeed(entry.getValue()))
                .log()
                .subscribe(trade ->
                        tradeFeed.tryEmitNext(trade)
                );

        return this;
    }

    private Flux<Trade> getTradeFeed(Strategy strategy) {
        return exchange
                .getTracker()
                .trackPair(String.format("%1s/%2s", strategy.getBase(), strategy.getQuote()))
                .getTickerFeed()
                .flatMap(tickerMessage -> getTargetPrice(tickerMessage, strategy))
                .map(price -> createTrade(strategy, price));
    }

    private Mono<BigDecimal> getTargetPrice(TickerMessage tickerMessage, Strategy strategy) {
        return exchange.getWebClient()
                .getAssetPairs(tickerMessage.getPair())
                .map(pairs -> pairs.getPairs().get(tickerMessage.getPair()))
                .flatMap(assetPair ->
                        Mono.fromCallable(() ->
                                        priceCalculator.targetPrice(
                                                tickerMessage,
                                                strategy,
                                                feeProcessor.fees(assetPair)
                                        ))
                                .subscribeOn(Schedulers.boundedElastic()));
    }

    private Trade createTrade(Strategy strategy, BigDecimal targetPrice) {
        return new Trade()
                .setType(StrategyType.valueOf(strategy.getType().toUpperCase().trim()))
                .setPair(String.format("%1s/%2s", strategy.getBase(), strategy.getQuote()))
                .setTargetPrice(targetPrice);
    }
}
