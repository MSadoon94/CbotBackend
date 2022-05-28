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
import org.springframework.messaging.handler.annotation.SendTo;
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
    @SendTo("/topic/trades")
    public Map<UUID, Trade> createTrade(Map<String, String> strategyDetails, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        Strategy strategy = modifyStrategy(user, strategyDetails);
        ExchangeName exchangeName = ExchangeName.valueOf(strategy.getExchange().toUpperCase());
        if(strategy.isActive() && !isAlreadyCreated(strategy, user)){
            addTrade(strategy, exchangeName, user);
        }

        return user.getTrades();
    }

    private Strategy modifyStrategy(User user, Map<String, String> strategyDetails){
        Strategy strategy = user.getStrategies()
                .get(strategyDetails.get("name"));
        strategy.setActive(Boolean.parseBoolean(strategyDetails.get("isActive")));
        userService.addStrategy(user, strategy);
        return strategy;
    }

    private boolean isAlreadyCreated(Strategy strategy, User user){
        return user.getTrades()
                .values()
                .stream()
                .anyMatch(trade -> trade.getStrategyName().equals(strategy.getName()));
    }

    private void addTrade(Strategy strategy, ExchangeName exchangeName, User user){

        Trade trade = new Trade()
                .setStrategyName(strategy.getName())
                .setExchange(exchangeName)
                .setStatus(TradeStatus.CREATION)
                .setPair(strategy.getPair())
                .setType(StrategyType.valueOf(strategy.getType().toUpperCase()))
                .setTimeFrame(strategy.getTimeFrame())
                .setTimeUnits(strategy.getTimeUnit());

        if(strategy.getEntry().isBlank()){
           trade = trade.setTargetPrice(new BigDecimal(strategy.getTargetPrice()));
        } else {
           trade = trade.setEntryPercentage(new BigDecimal(strategy.getEntry()).movePointLeft(2));
        }


        tradeListener.start(user, exchangeSupplier.getExchange(exchangeName))
                .subscribe();
        userService.addTrade(user, trade);
    }

}
