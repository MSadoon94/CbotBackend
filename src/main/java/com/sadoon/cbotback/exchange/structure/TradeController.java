package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class TradeController {
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;

    public TradeController(SimpMessagingTemplate messagingTemplate,
                           UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }


    @SubscribeMapping("/trades")
    public void tradeFeed(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        userService.getTradeFeeds(user)
                .thenMany(userService.getTradeFeed(user)
                )
                .subscribe(trade -> {

                    userService.addTrade(user, trade);

                    messagingTemplate.convertAndSend(
                            "/topic/trade-feed",
                            trade
                    );

                });
    }

    @MessageMapping("/{name}/create-trade")
    public Trade createTrade(@DestinationVariable("name") String strategyName, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());

        Strategy strategy = user.getStrategies()
                .get(strategyName);

        Trade trade = new Trade()
                .setExchange(ExchangeName.valueOf(strategy.getExchange().toUpperCase()))
                .setStatus(TradeStatus.CREATION)
                .setPair(strategy.getPair())
                .setType(StrategyType.valueOf(strategy.getType().toUpperCase()));
        userService.addTrade(user, trade);

        return trade;
    }

}
