package com.sadoon.cbotback.exchange.model;

public interface TickerMessage {

    String getPair();
    String getAsk();
    String getBid();
    String getOpen();
    String getClose();
    String getHigh();
    String getLow();
    String getVolume();

}