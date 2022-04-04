package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.strategy.StrategyType;
import reactor.core.publisher.Flux;

public class EntryScanner {

    public Flux<Trade> tradeFeed(Flux<Trade> tradeFeedIn) {
        return tradeFeedIn
                .map(trade -> trade.setID(
                        String.join("",
                                trade.getCurrentPrice().toString(),
                                trade.getPair(),
                                trade.getTargetPrice().toString(),
                                trade.getType().name()
                        )))
                .map(trade -> {
                            if (isTradeWithinRangeOfTarget(trade)) {
                                trade.setStatus(TradeStatus.ENTRY_FOUND);
                            } else {
                                trade.setStatus(TradeStatus.ENTRY_SEARCHING);
                            }
                            return trade;
                        }
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
