package com.sadoon.cbotback.trade;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.structure.ExchangeUtil;
import com.sadoon.cbotback.executor.EntryScanner;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
public class TradeListener {
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;

    public TradeListener(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    public Flux<Trade> start(User user,
                             ExchangeUtil exchangeUtil) {
        handleTradeUpdates(user, exchangeUtil);
        return userService.userTradeFeed(user)
                .filter(trade -> trade.getStatus() == TradeStatus.CREATION)
                .filter(trade -> exchangeUtil.getExchangeName() == trade.getExchange())
                .transform(exchangeUtil.getTracker().addCurrentPrice(user)
                        .andThen(exchangeUtil.getWebClient().addAllPairNames())
                        .andThen(exchangeUtil.getWebClient().addFees(
                                userService.getCredential(user, exchangeUtil.getExchangeName().name())))
                        .andThen(exchangeUtil.getPriceCalculator().addTargetPrice)
                        .andThen(exchangeUtil.getEntryScanner().findEntry(userService, user)))
                .onErrorResume(Mono::error);
    }

    private void handleTradeUpdates(User user, ExchangeUtil exchangeUtil) {
        userService.
                userTradeUpdateFeed(user)
                .subscribe(
                        trade ->
                        {
                            trade = checkForEntryFound(exchangeUtil.getEntryScanner()).apply(trade);
                            messagingTemplate.convertAndSend(
                                    String.format("/topic/trade/%s/update", trade.getId()),
                                    trade
                            );
                        });
    }

    private UnaryOperator<Trade> checkForEntryFound(EntryScanner entryScanner) {
        return trade -> {
            if (entryScanner.isTradeWithinRangeOfTarget(trade)) {
                trade = trade.setStatus(TradeStatus.ENTRY_FOUND);
            }
            return trade;
        };
    }
}
