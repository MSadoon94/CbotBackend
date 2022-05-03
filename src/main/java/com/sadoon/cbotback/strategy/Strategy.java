package com.sadoon.cbotback.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sadoon.cbotback.exceptions.notfound.StrategyTypeNotFoundException;

import java.util.Arrays;

public class Strategy {
    private boolean isActive = false;
    private String name;
    private String type;
    private String exchange;
    private String base;
    private String quote;
    private String stopLoss;
    private String maxPosition;
    private String targetProfit;
    private String movingStopLoss;
    private String maxLoss;
    private String entry;
    private String timeFrame;
    private String timeUnit;

    @JsonIgnore
    public String getPair() {
        return String.format("%1s/%2s", base, quote);
    }

    public boolean isActive() {
        return isActive;
    }

    @JsonSetter("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public String getMaxPosition() {
        return maxPosition;
    }

    public void setMaxPosition(String maxPosition) {
        this.maxPosition = maxPosition;
    }

    public String getTargetProfit() {
        return targetProfit;
    }

    public void setTargetProfit(String targetProfit) {
        this.targetProfit = targetProfit;
    }

    public String getMovingStopLoss() {
        return movingStopLoss;
    }

    public void setMovingStopLoss(String movingStopLoss) {
        this.movingStopLoss = movingStopLoss;
    }

    public String getMaxLoss() {
        return maxLoss;
    }

    public void setMaxLoss(String maxLoss) {
        this.maxLoss = maxLoss;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public StrategyType asStrategyType() throws StrategyTypeNotFoundException {
        return Arrays.stream(StrategyType.values())
                .filter(strategyType -> strategyType.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new StrategyTypeNotFoundException(type));
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }
}
