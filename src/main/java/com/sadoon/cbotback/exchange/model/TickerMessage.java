package com.sadoon.cbotback.exchange.model;

import com.sadoon.cbotback.strategy.StrategyType;

public interface TickerMessage {

    default String getPrice(StrategyType type) {
        if (type.equals(StrategyType.LONG)) {
            return getBid();
        } else {
            return getAsk();
        }
    }

    String getPair();

    String getAsk();

    String getBid();

    String getOpen();

    String getClose();

    String getHigh();

    String getLow();

    String getVolume();

}