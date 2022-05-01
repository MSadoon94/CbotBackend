package com.sadoon.cbotback.update;

import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.user.UserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class UserUpdater {
    private ExchangeSupplier exchangeSupplier;
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;

    public UserUpdater(ExchangeSupplier exchangeSupplier,
                       SimpMessagingTemplate messagingTemplate,
                       UserService userService) {
        this.exchangeSupplier = exchangeSupplier;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    public void addBalanceUpdates(ExchangeName exchangeName, Principal principal, Flux<Long> interval) {
        Exchange exchange = exchangeSupplier.getExchange(exchangeName);
        getBalances(exchange, exchangeName, principal)
                .subscribe(sendBalancesUpdate(exchangeName));
        interval
                .flatMap(time -> getBalances(exchange, exchangeName, principal))
                .subscribe(sendBalancesUpdate(exchangeName));
    }

    private Flux<Balances> getBalances(Exchange exchange, ExchangeName exchangeName, Principal principal) {
        return Mono.fromCallable(() -> exchange.getWebClient()
                        .balances(userService.getCredential(
                                userService.getUserWithUsername(principal.getName()),
                                exchangeName.name())))
                .flux()
                .flatMap(Function.identity());
    }

    private Consumer<Balances> sendBalancesUpdate(ExchangeName exchangeName) {
        return balances ->
                messagingTemplate.convertAndSend(
                        "/topic/balance",
                        new BalanceUpdate(exchangeName.name(), balances));
    }
}