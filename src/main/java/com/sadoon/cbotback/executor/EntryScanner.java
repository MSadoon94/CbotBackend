package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import reactor.core.publisher.Flux;

import java.util.function.UnaryOperator;

public class EntryScanner {

    public UnaryOperator<Flux<Trade>> findEntry(UserService userService, User user) {
        return tradeFeedIn -> tradeFeedIn
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
                        trade = trade.setStatus(TradeStatus.ENTRY_FOUND);
                    } else {
                        trade = trade.setStatus(TradeStatus.ENTRY_SEARCHING);
                    }
                    return trade;
                })
                .doOnNext(trade -> userService.updateTrade(user, trade));
    }


    private boolean isTradeWithinRangeOfTarget(Trade trade) {
        if (trade.getType().equals(StrategyType.LONG)) {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) <= 0;
        } else {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) >= 0;
        }
    }
}
