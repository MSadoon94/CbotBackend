package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exp.Trade;
import com.sadoon.cbotback.strategy.StrategyType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class EntryScanner {
    private Exchange exchange;
    private ExecutorService entryScannerExecutor;

    private Sinks.Many<Trade> potentialTrades = Sinks.many().multicast().onBackpressureBuffer();

    public EntryScanner(Exchange exchange, ExecutorService entryScannerExecutor) {
        this.exchange = exchange;
        this.entryScannerExecutor = entryScannerExecutor;
    }

    public Flux<Trade> getPotentialTrades() {
        return potentialTrades.asFlux();
    }

    public void addTradesToScan(List<Trade> trades) {
        AssetTracker tracker = exchange.getTracker();
        trades.forEach(
                trade ->
                        entryScannerExecutor.execute(() ->
                                tracker.trackPair(trade.getPair())
                                        .getTickerFeed()
                                        .map(tickerMessage -> trade
                                                .setCurrentPrice(
                                                        new BigDecimal(tickerMessage.getPrice(trade.getType())))
                                        )
                                        .filter(tickerMessage -> isTradeWithinRangeOfTarget(trade))
                                        .subscribe(tickerMessage -> potentialTrades.tryEmitNext(trade)))
        );

    }

    private boolean isTradeWithinRangeOfTarget(Trade trade) {
        if (trade.getType().equals(StrategyType.LONG)) {
            return trade.getTargetPrice().compareTo(trade.getCurrentPrice()) <= 0;
        } else {
            return trade.getTargetPrice().compareTo(trade.getCurrentPrice()) >= 0;
        }
    }
}
