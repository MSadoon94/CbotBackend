package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exchange.model.TickerMessage;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExchangeMessageFactory {

    Mono<String> tickerSubscribe(List<String> pairs);

    <T extends TickerMessage> Mono<T> tickerMessage(String message);

}