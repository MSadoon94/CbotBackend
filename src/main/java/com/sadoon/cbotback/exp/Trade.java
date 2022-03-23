package com.sadoon.cbotback.exp;

import com.sadoon.cbotback.strategy.StrategyType;

import java.math.BigDecimal;

public class Trade {
    private String pair;
    private BigDecimal targetPrice;
    private BigDecimal currentPrice;
    private StrategyType type;

    public String getPair() {
        return pair;
    }

    public Trade setPair(String pair) {
        this.pair = pair;
        return this;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public Trade setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
        return this;
    }

    public StrategyType getType() {
        return type;
    }

    public Trade setType(StrategyType type) {
        this.type = type;
        return this;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public Trade setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        return this;
    }
}