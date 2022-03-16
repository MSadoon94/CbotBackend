package com.sadoon.cbotback.exchange;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.exchange.model.ExchangeCredentials;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import reactor.core.publisher.Mono;

public interface ExchangeWebClient {

    Mono<TradeVolume> getTradeVolume(ExchangeCredentials credentials,String pair);
    Mono<AssetPairs> getAssetPairs(String pair);
}