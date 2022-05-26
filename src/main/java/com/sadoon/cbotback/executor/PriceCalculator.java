package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class PriceCalculator {

    public UnaryOperator<Flux<Trade>> addTargetPrice = tradeFeedIn ->
            tradeFeedIn
                    .flatMap(trade -> Mono
                            .fromCallable(() -> addTargetPrice(trade))
                            .subscribeOn(Schedulers.boundedElastic()));

    private Trade addTargetPrice(Trade trade) throws OutOfBoundsException {
        if (trade.getTargetPrice() == null ||trade.getTargetPrice().equals(BigDecimal.ZERO)) {
            trade = calculateTargetPrice(trade);
        }

        return trade;
    }

    private Trade calculateTargetPrice(Trade trade) throws OutOfBoundsException {
        BigDecimal target = BigDecimal.ZERO;

        if (trade.getType().equals(StrategyType.LONG)) {

            //Target = (CurrentPrice - (PercentageOfCurrentPriceAsDecimal * 100)) - TotalFeesCost

            target =
                    trade.getCurrentPrice()
                            .subtract((checkEntryBounds(trade.getEntryPercentage())
                                    .movePointLeft(2))
                                    .multiply(trade.getCurrentPrice()))
                            .subtract(new BigDecimal(trade.getFees().getFee()));
        } else if (trade.getType().equals(StrategyType.SHORT)) {

            //Target = (CurrentPrice + (PercentageOfCurrentPriceAsDecimal * 100)) + TotalFeesCost

            target =
                    trade.getCurrentPrice()
                            .add((checkEntryBounds(trade.getEntryPercentage())
                                    .movePointLeft(2))
                                    .multiply(trade.getCurrentPrice())
                                    .add(new BigDecimal(trade.getFees().getFee())));
        }
        return trade.setTargetPrice(target);

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