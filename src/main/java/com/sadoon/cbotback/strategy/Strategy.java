package com.sadoon.cbotback.strategy;

public class Strategy {
    private String name;
    private String base;
    private String quote;
    private String stopLoss;
    private String maxPosition;
    private String targetProfit;
    private String movingStopLoss;
    private String maxLoss;
    private String longEntry;

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

    public String getLongEntry() {
        return longEntry;
    }

    public void setLongEntry(String longEntry) {
        this.longEntry = longEntry;
    }
}
