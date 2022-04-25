package com.sadoon.cbotback.trade;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class TradeController {

    private UserService userService;
    private SimpMessagingTemplate messagingTemplate;

    public TradeController(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }


    @SubscribeMapping("/trades")
    public Map<UUID, Trade> tradeFeed(Principal principal) throws UserNotFoundException {
        userService.getUpdatedTrades()
                .subscribe(trade ->
                        messagingTemplate.convertAndSend(
                                String.format("/topic/%s/update", trade.getId().toString()),
                                trade
                        )
                );
        return userService.getUserWithUsername(principal.getName())
                .getTrades();
    }

    @MessageMapping("/create-trade")
    public Trade createTrade(String strategyName, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());

        Strategy strategy = user.getStrategies()
                .get(strategyName);

        Trade trade = new Trade()
                .setStrategyName(strategyName)
                .setExchange(ExchangeName.valueOf(strategy.getExchange().toUpperCase()))
                .setStatus(TradeStatus.CREATION)
                .setPair(strategy.getPair())
                .setType(StrategyType.valueOf(strategy.getType().toUpperCase()));

        userService.addTrade(user, trade);

        return trade;
    }

}
