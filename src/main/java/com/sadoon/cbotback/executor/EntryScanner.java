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
                                        trade.getType().name()))
                        .setStatus(TradeStatus.ENTRY_SEARCHING))
                .takeWhile(trade -> userService.doesUserExist(user))
                .doOnNext(trade -> userService.updateTrade(user, trade));
    }

    public boolean isTradeWithinRangeOfTarget(Trade trade) {
        if (trade.getType().equals(StrategyType.LONG)) {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) <= 0;
        } else {
            return trade.getCurrentPrice().compareTo(trade.getTargetPrice()) >= 0;
        }
    }
}
