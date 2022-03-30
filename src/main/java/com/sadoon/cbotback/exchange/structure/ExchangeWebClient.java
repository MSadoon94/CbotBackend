package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExchangeWebClient {

    Mono<TradeVolume> tradeVolume(SecurityCredentials credentials, List<String> pair);

    Mono<AssetPairs> assetPairs(String pairs);

    Flux<Trade> assetPairTradeFeed(Flux<Trade> tradeFeedIn);

    Flux<Trade> tradeVolumeTradeFeed(SecurityCredentials credentials, Flux<Trade> tradeFeedIn);
}