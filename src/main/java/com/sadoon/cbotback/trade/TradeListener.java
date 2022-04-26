package com.sadoon.cbotback.trade;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TradeListener {
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;

    public TradeListener(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    public Flux<Trade> start(User user,
                             Exchange exchange) {
        handleTradeUpdates(user);
        return userService.userTradeFeed(user)
                .filter(trade -> trade.getStatus() == TradeStatus.CREATION)
                .filter(trade -> exchange.getExchangeName() == trade.getExchange())
                .transform(exchange.getTracker().addCurrentPrice(userService, user)
                        .andThen(exchange.getWebClient().addAllPairNames())
                        .andThen(exchange.getWebClient().addFees(
                                userService.getCredential(user, exchange.getExchangeName().name())))
                        .andThen(exchange.getPriceCalculator().addTargetPrice)
                        .andThen(exchange.getEntryScanner().findEntry(userService, user)))
                .onErrorResume(Mono::error);
    }

    private void handleTradeUpdates(User user) {
        userService.
                userTradeUpdateFeed(user)
                .subscribe(
                        trade ->
                                messagingTemplate.convertAndSend(
                                        String.format("/topic/trade/%s/update", trade.getId()),
                                        trade
                                ));
    }
}
