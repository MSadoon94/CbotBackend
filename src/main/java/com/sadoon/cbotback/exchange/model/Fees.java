package com.sadoon.cbotback.exchange.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Fees {
    @JsonIgnore
    private String pair;
    private String fee;
    @JsonAlias("minfee")
    private String minFee;
    @JsonAlias("maxfee")
    private String maxFee;
    @JsonAlias("nextfee")
    private String nextFee;
    @JsonAlias("nextvolume")
    private String nextVolume;
    @JsonAlias("tiervolume")
    private String tierVolume;


    public String getPair() {
        return pair;
    }

    public Fees setPair(String pair) {
        this.pair = pair;
        return this;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getMinFee() {
        return minFee;
    }

    public void setMinFee(String minFee) {
        this.minFee = minFee;
    }

    public String getMaxFee() {
        return maxFee;
    }

    public void setMaxFee(String maxFee) {
        this.maxFee = maxFee;
    }

    public String getNextFee() {
        return nextFee;
    }

    public void setNextFee(String nextFee) {
        this.nextFee = nextFee;
    }

    public String getNextVolume() {
        return nextVolume;
    }

    public void setNextVolume(String nextVolume) {
        this.nextVolume = nextVolume;
    }

    public String getTierVolume() {
        return tierVolume;
    }

    public void setTierVolume(String tierVolume) {
        this.tierVolume = tierVolume;
    }
}