package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AssetController {

    private ExchangeSupplier exchangeSupplier;
    private AssetPairs pairCache;
    private List<String> pairs = new ArrayList<>();

    public AssetController(ExchangeSupplier exchangeSupplier) {
        this.exchangeSupplier = exchangeSupplier;
    }

    @MessageMapping("/asset-pairs")
    public AssetPairs assetPairs(@Payload AssetPairMessage message) {
        Exchange exchange = exchangeSupplier.getExchange(
                ExchangeName.valueOf(message.getExchange().toUpperCase()));
        if ((pairCache == null) || (hasBeenValidated(message.getAssets()))) {
            pairs.add(message.getAssets());
            pairCache = exchange.getWebClient()
                    .assetPairs(String.join(",", pairs))
                    .block();
        }
        return pairCache;
    }

    private boolean hasBeenValidated(String pair) {
        if (pairCache.getPairs() == null) {
            return false;
        } else {
            return !pairCache.getPairs().containsKey(pair);
        }
    }
}
