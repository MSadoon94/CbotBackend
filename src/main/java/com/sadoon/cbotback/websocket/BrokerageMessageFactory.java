package com.sadoon.cbotback.websocket;

import com.sadoon.cbotback.asset.AssetPairs;
import reactor.core.publisher.Mono;

public interface BrokerageMessageFactory {

    Mono<String> tickerSubscribe(AssetPairs assetPairs);

    Mono<? extends TickerMessage> tickerMessage(String message);

}