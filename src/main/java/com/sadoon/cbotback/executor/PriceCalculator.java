package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exceptions.notfound.StrategyTypeNotFoundException;
import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;

import java.math.BigDecimal;

public class PriceCalculator {

    public BigDecimal targetPrice(TickerMessage ticker,
                                  Strategy strategy,
                                  Fees fees

    ) throws StrategyTypeNotFoundException, OutOfBoundsException {
        BigDecimal target = BigDecimal.ZERO;

        if (strategy.asStrategyType().equals(StrategyType.LONG)) {
            target =
                    new BigDecimal(ticker.getBid())
                            .subtract((checkEntryBounds(new BigDecimal(strategy.getEntry()))
                                    .movePointLeft(2))
                                    .multiply(new BigDecimal(ticker.getBid())))
                            .subtract(new BigDecimal(fees.getFee()));
        } else if (strategy.asStrategyType().equals(StrategyType.SHORT)) {
            target =
                    new BigDecimal(ticker.getAsk())
                            .add((checkEntryBounds(new BigDecimal(strategy.getEntry()))
                                    .movePointLeft(2))
                                    .multiply(new BigDecimal(ticker.getAsk())))
                            .add(new BigDecimal(fees.getFee()));
        }

        return target;
    }

    private BigDecimal checkEntryBounds(BigDecimal entry) throws OutOfBoundsException {
        if (entry.compareTo(BigDecimal.ZERO) <= 0
                || entry.compareTo(new BigDecimal("100")) >= 0) {
            throw new OutOfBoundsException(entry.toPlainString(), "0-100");
        } else {
            return entry;
        }
    }
}