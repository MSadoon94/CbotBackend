package com.sadoon.cbotback.exchange;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.security.SecurityCredentials;
import reactor.core.publisher.Mono;

public interface ExchangeWebClient {

    Mono<TradeVolume> getTradeVolume(SecurityCredentials credentials, String pair);

    Mono<AssetPairs> getAssetPairs(String pair);
}