package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import reactor.core.publisher.Flux;

import java.util.function.UnaryOperator;

public class EntryScanner {

    public UnaryOperator<Flux<Trade>> findEntry = tradeFeedIn -> tradeFeedIn
            .map(trade -> trade.setLabel(
                    String.join("",
                            trade.getExchange().name(),
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

    private boolean isTradeWithinRangeOfTarget(Trade trade) {
        if (trade.getType().equals(StrategyType.LONG)) {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) <= 0;
        } else {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) >= 0;
        }
    }
}
