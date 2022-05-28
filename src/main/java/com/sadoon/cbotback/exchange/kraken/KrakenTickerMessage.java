package com.sadoon.cbotback.exchange.kraken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sadoon.cbotback.exchange.model.TickerMessage;

import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenTickerMessage implements TickerMessage {
    @JsonIgnore
    private String pair;
    @JsonIgnore
    private long timestamp;
    private String[] ask;
    private String[] bid;
    private String[] open;
    private String[] close;
    private String[] high;
    private String[] low;
    private String[] volume;


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getPair() {
        return pair;
    }

    @Override
    public String getAsk() {
        return ask[0];
    }

    @Override
    public String getBid() {
        return bid[0];
    }

    @Override
    public String getOpen() {
        return open[0];
    }

    @Override
    public String getClose() {
        return close[0];
    }

    @Override
    public String getHigh() {
        return high[0];
    }

    @Override
    public String getLow() {
        return low[0];
    }

    @Override
    public String getVolume() {
        return volume[0];
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public void setTimestamp(){
        timestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }

    @JsonSetter("a")
    public void setAsk(String[] ask) {
        this.ask = ask;
    }

    @JsonSetter("b")
    public void setBid(String[] bid) {
        this.bid = bid;
    }

    @JsonSetter("o")
    public void setOpen(String[] open) {
        this.open = open;
    }

    @JsonSetter("c")
    public void setClose(String[] close) {
        this.close = close;
    }

    @JsonSetter("h")
    public void setHigh(String[] high) {
        this.high = high;
    }

    @JsonSetter("l")
    public void setLow(String[] low) {
        this.low = low;
    }

    @JsonSetter("v")
    public void setVolume(String[] volume) {
        this.volume = volume;
    }
}