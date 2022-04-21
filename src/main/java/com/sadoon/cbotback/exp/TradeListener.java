package com.sadoon.cbotback.exp;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;

public class TradeListener {
    private UserService userService;
    private Exchange exchange;
    private User user;


    public TradeListener(UserService userService, Exchange exchange, User user) {
        this.userService = userService;
        this.exchange = exchange;
        this.user = user;
    }

    public void start() {
        user.getTradeFeed2()
                .filter(trade -> trade.getStatus() != TradeStatus.CREATION)
                .filter(trade -> exchange.getExchangeName() == trade.getExchange())
                .transform(exchange.getTracker().addCurrentPrice
                        .andThen(exchange.getWebClient().addAllPairNames())
                        .andThen(exchange.getWebClient().addFees(
                                userService.getCredential(user,
                                        exchange.getExchangeName().name())))
                        .andThen(exchange.getPriceCalculator().addTargetPrice)
                        .andThen(exchange.getEntryScanner().findEntry))
                .subscribe(trade -> userService.addTrade(user, trade));
    }
}
