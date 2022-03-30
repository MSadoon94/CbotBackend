package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.strategy.StrategyType;
import reactor.core.publisher.Flux;

public class EntryScanner {

    public Flux<Trade> tradeFeed(Flux<Trade> tradeFeedIn) {
        return tradeFeedIn
                .filter(this::isTradeWithinRangeOfTarget);
    }

    private boolean isTradeWithinRangeOfTarget(Trade trade) {
        if (trade.getType().equals(StrategyType.LONG)) {
            return trade.getTargetPrice().compareTo(trade.getCurrentPrice()) <= 0;
        } else {
            return trade.getTargetPrice().compareTo(trade.getCurrentPrice()) >= 0;
        }
    }
}
