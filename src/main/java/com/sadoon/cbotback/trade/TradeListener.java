package com.sadoon.cbotback.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class TradeListener {
    public Flux<Trade> start(Flux<Trade> tradeFeedIn,
                             Map<String, SecurityCredential> credentials,
                             Exchange exchange) {
        return tradeFeedIn
                .log()
                /*.filter(trade -> trade.getStatus() != TradeStatus.CREATION)*/
                .filter(trade -> exchange.getExchangeName() == trade.getExchange())
                .transform(exchange.getTracker().addCurrentPrice
                        .andThen(exchange.getWebClient().addAllPairNames())
                        .andThen(exchange.getWebClient().addFees(
                                credentials.get(exchange.getExchangeName().name())))
                        .andThen(exchange.getPriceCalculator().addTargetPrice)
                        .andThen(exchange.getEntryScanner().findEntry))
                .onErrorResume(Mono::error);
    }
}
