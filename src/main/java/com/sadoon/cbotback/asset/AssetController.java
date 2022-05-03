package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.structure.ExchangeUtil;
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
        ExchangeUtil exchangeUtil = exchangeSupplier.getExchange(
                ExchangeName.valueOf(message.getExchange().toUpperCase()));
        if ((pairCache == null) || (hasNotBeenValidated(message.getAssets()))) {
            pairs.add(message.getAssets());
            pairCache = exchangeUtil.getWebClient()
                    .assetPairs(String.join(",", pairs))
                    .block();
        }
        return pairCache;
    }

    private boolean hasNotBeenValidated(String pair) {
        if (pairCache.getPairs() == null) {
            return true;
        } else {
            return !pairCache.getPairs().containsKey(pair);
        }
    }
}
