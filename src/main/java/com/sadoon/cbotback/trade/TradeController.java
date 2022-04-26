package com.sadoon.cbotback.trade;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class TradeController {

    private TradeListener tradeListener;
    private ExchangeSupplier exchangeSupplier;
    private UserService userService;

    public TradeController(TradeListener tradeListener,
                           ExchangeSupplier exchangeSupplier,
                           UserService userService) {
        this.tradeListener = tradeListener;
        this.exchangeSupplier = exchangeSupplier;
        this.userService = userService;
    }

    @SubscribeMapping("/trades")
    public Map<UUID, Trade> tradeFeed(Principal principal) throws UserNotFoundException {
        return userService.getUserWithUsername(principal.getName())
                .getTrades();
    }

    @MessageMapping("/create-trade")
    public Trade createTrade(String strategyName, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        Strategy strategy = user.getStrategies()
                .get(strategyName);
        ExchangeName exchangeName = ExchangeName.valueOf(strategy.getExchange().toUpperCase());

        Trade trade = new Trade()
                .setStrategyName(strategyName)
                .setExchange(exchangeName)
                .setStatus(TradeStatus.CREATION)
                .setPair(strategy.getPair())
                .setType(StrategyType.valueOf(strategy.getType().toUpperCase()))
                .setEntryPercentage(new BigDecimal(strategy.getEntry()).movePointLeft(2));

        userService.addTrade(user, trade);
        tradeListener.start(user, exchangeSupplier.getExchange(exchangeName))
                .subscribe();

        return trade;
    }

}
