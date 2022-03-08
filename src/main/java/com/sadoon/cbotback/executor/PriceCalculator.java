package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetPair;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exceptions.notfound.StrategyTypeNotFoundException;
import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.websocket.TickerMessage;

import java.math.BigDecimal;
import java.util.Map;

public class PriceCalculator {

    private Map<String, AssetTracker> trackers;

    public PriceCalculator(Map<String, AssetTracker> trackers) {
        this.trackers = trackers;
    }

    public BigDecimal targetPrice(Strategy strategy) throws StrategyTypeNotFoundException, OutOfBoundsException {
        checkEntryBounds(new BigDecimal(strategy.getEntry()));
        AssetTracker tracker = trackers.get(strategy.getBrokerage());

        TickerMessage ticker =
                tracker.getTickerFlux(assetPairs(strategy)).blockFirst().block();

        BigDecimal target;
        if (strategy.getType().equals(StrategyType.LONG.name())) {
            target =
                    (new BigDecimal(strategy.getEntry()).movePointLeft(2))
                            .multiply(new BigDecimal(ticker.getBid()));
        } else if (strategy.getType().equals(StrategyType.SHORT.name())) {
            target =
                    (new BigDecimal(strategy.getEntry()).movePointLeft(2))
                            .multiply(new BigDecimal(ticker.getAsk()));
        } else {
            throw new StrategyTypeNotFoundException(strategy.getType());
        }

        return target;
    }

    private AssetPairs assetPairs(Strategy strategy) {
        AssetPairs pairs = new AssetPairs();
        AssetPair assetPair = new AssetPair();
        assetPair.setBase(strategy.getBase());
        assetPair.setQuote(strategy.getQuote());

        pairs.setPairs(Map.of(
                String.format("%1s/%2s", assetPair.getBase(), assetPair.getQuote()),
                assetPair
        ));
        return pairs;
    }

    private void checkEntryBounds(BigDecimal entry) throws OutOfBoundsException {
        if(entry.compareTo(BigDecimal.ZERO) <= 0
                || entry.compareTo(new BigDecimal("100")) >= 0){
            throw new OutOfBoundsException(entry.toPlainString(), "0-100");
        }
    }
}