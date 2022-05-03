package com.sadoon.cbotback.update;

import com.sadoon.cbotback.exchange.model.Balances;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.model.Exchange;
import com.sadoon.cbotback.exchange.structure.ExchangeUtil;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.user.UserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;
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
        ExchangeUtil exchangeUtil = exchangeSupplier.getExchange(exchangeName);
        sendInitialBalance(exchangeUtil, exchangeName, principal);
        interval
                .flatMap(time -> getBalances(exchangeUtil, exchangeName, principal))
                .subscribe(sendBalancesUpdate(exchangeName), sendRejectedMessage(exchangeName));
    }

    private void sendInitialBalance(ExchangeUtil exchangeUtil, ExchangeName exchangeName, Principal principal){
        getBalances(exchangeUtil, exchangeName, principal)
                .subscribe(sendBalancesUpdate(exchangeName), sendRejectedMessage(exchangeName));
    }

    private Flux<Balances> getBalances(ExchangeUtil exchangeUtil, ExchangeName exchangeName, Principal principal) {
        return Mono.fromCallable(() -> exchangeUtil.getWebClient()
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
                        new Exchange(exchangeName.name(), balances));
    }
    private Consumer<Throwable> sendRejectedMessage(ExchangeName exchangeName){
        return error -> messagingTemplate.convertAndSend(
                "/topic/rejected-credentials",
                Map.of(
                        "exchange", exchangeName,
                        "message", error.getMessage())
        );
    }
}