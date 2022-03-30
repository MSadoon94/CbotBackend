package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exchange.model.TickerMessage;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BrokerageMessageFactory {

    Mono<String> tickerSubscribe(List<String> pairs);

    Mono<? extends TickerMessage> tickerMessage(String message);

}