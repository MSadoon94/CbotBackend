package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetPair;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exceptions.notfound.EntityNotFoundException;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.TradeVolume;

public class FeeProcessor {
    private Exchange exchange;

    public FeeProcessor(Exchange exchange) {
        this.exchange = exchange;
    }

    public Fees fees(AssetPair pair) throws ExchangeRequestException, EntityNotFoundException {
        String combinedPair = pair.getBase().concat(pair.getQuote());
        return exchange.getResponseHandler()
                .getFees(tradeVolume(combinedPair))
                .stream()
                .filter(fee ->
                        fee.getPair().equals(combinedPair)
                                || fee.getPair().equals(pair.getWsName())
                                || fee.getPair().equals(pair.getAltName())
                )
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(String.format("Fees for %1s", combinedPair)));
    }

    private TradeVolume tradeVolume(String pair) {
        return exchange.getWebClient()
                .getTradeVolume(exchange.getCredentials(), pair)
                .block();
    }
}