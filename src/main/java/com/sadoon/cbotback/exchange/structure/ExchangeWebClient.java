package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.UnaryOperator;

public interface ExchangeWebClient {

    Flux<Balances> balances(SecurityCredential credentials);

    Mono<TradeVolume> tradeVolume(SecurityCredential credentials, List<String> pair);

    Mono<AssetPairs> assetPairs(String pairs);

    Flux<Trade> assetPairTradeFeed(Flux<Trade> tradeFeedIn);

    Flux<Trade> tradeVolumeTradeFeed(SecurityCredential credentials, Flux<Trade> tradeFeedIn);

    UnaryOperator<Flux<Trade>> addAllPairNames();

    UnaryOperator<Flux<Trade>> addFees(SecurityCredential credential);
}