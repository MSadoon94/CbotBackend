package com.sadoon.cbotback.exchange.model;

import com.sadoon.cbotback.strategy.StrategyType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Trade {
    private boolean isActive = false;
    private String pair;
    private List<String> allNames = new ArrayList<>();
    private BigDecimal targetPrice;
    private BigDecimal currentPrice;
    private BigDecimal entryPercentage;
    private Fees fees;
    private StrategyType type;

    public String getId(){
        return String.join("",
                pair,
                targetPrice.toString(),
                currentPrice.toString(),
                type.name());
    }

    public Fees getFees() {
        return fees;
    }

    public Trade setFees(Fees fees) {
        this.fees = fees;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public Trade setActive(boolean active) {
        isActive = active;
        return this;
    }

    public List<String> getAllNames() {
        return allNames;
    }

    public Trade addPairNames(List<String> names) {
        allNames.addAll(names);
        return this;
    }

    public Trade setAllNames(List<String> allNames){
        this.allNames = allNames;
        return this;
    }

    public String getPair() {
        return pair;
    }

    public Trade setPair(String pair) {
        allNames.add(pair);
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

    public BigDecimal getEntryPercentage() {
        return entryPercentage;
    }

    public Trade setEntryPercentage(BigDecimal entryPercentage) {
        this.entryPercentage = entryPercentage;
        return this;
    }
}