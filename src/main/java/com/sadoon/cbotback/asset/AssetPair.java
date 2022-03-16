package com.sadoon.cbotback.asset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssetPair {

    private String altName, wsName,  base, quote, baseType, quoteType, feeVolumeCurrency, orderMin;

    private Integer pairDecimals, lotDecimals, lotMultiplier, marginCall, marginStop;

    private Integer[] leverageBuy, leverageSell;

    private List<BigDecimal[]> feeSchedule;
    private List<BigDecimal[]> makerTakerFees;

    public String getAltName() {
        return altName;
    }

    @JsonAlias("altname")
    public void setAltName(String altName) {
        this.altName = altName;
    }

    public String getWsName() {
        return wsName;
    }

    @JsonAlias("wsname")
    public void setWsName(String wsName) {
        this.wsName = wsName;
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

    public String getBaseType() {
        return baseType;
    }

    @JsonAlias("aclass_base")
    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public String getQuoteType() {
        return quoteType;
    }

    @JsonAlias("aclass_quote")
    public void setQuoteType(String quoteType) {
        this.quoteType = quoteType;
    }

    public String getFeeVolumeCurrency() {
        return feeVolumeCurrency;
    }

    @JsonAlias("fee_volume_currency")
    public void setFeeVolumeCurrency(String feeVolumeCurrency) {
        this.feeVolumeCurrency = feeVolumeCurrency;
    }

    public String getOrderMin() {
        return orderMin;
    }

    public void setOrderMin(String orderMin) {
        this.orderMin = orderMin;
    }

    public Integer getPairDecimals() {
        return pairDecimals;
    }

    @JsonAlias("pair_decimals")
    public void setPairDecimals(Integer pairDecimals) {
        this.pairDecimals = pairDecimals;
    }

    public Integer getLotDecimals() {
        return lotDecimals;
    }

    @JsonAlias("lot_decimals")
    public void setLotDecimals(Integer lotDecimals) {
        this.lotDecimals = lotDecimals;
    }

    public Integer getLotMultiplier() {
        return lotMultiplier;
    }

    @JsonAlias("lot_multiplier")
    public void setLotMultiplier(Integer lotMultiplier) {
        this.lotMultiplier = lotMultiplier;
    }

    public Integer getMarginCall() {
        return marginCall;
    }

    @JsonAlias("margin_call")
    public void setMarginCall(Integer marginCall) {
        this.marginCall = marginCall;
    }

    public Integer getMarginStop() {
        return marginStop;
    }

    @JsonAlias("margin_stop")
    public void setMarginStop(Integer marginStop) {
        this.marginStop = marginStop;
    }

    public Integer[] getLeverageBuy() {
        return leverageBuy;
    }

    @JsonAlias("leverage_buy")
    public void setLeverageBuy(Integer[] leverageBuy) {
        this.leverageBuy = leverageBuy;
    }

    public Integer[] getLeverageSell() {
        return leverageSell;
    }

    @JsonAlias("leverage_sell")
    public void setLeverageSell(Integer[] leverageSell) {
        this.leverageSell = leverageSell;
    }

    @JsonProperty("fees")
    public void setFeeSchedule(List<String[]> feeSchedule) {
        this.feeSchedule = convertToBigDecimal(feeSchedule);
    }

    @JsonProperty("fees_maker")
    public void setMakerTakerFees(List<String[]> makerTakerFees) {
        this.makerTakerFees = convertToBigDecimal(makerTakerFees);
    }

    private List<BigDecimal[]> convertToBigDecimal(List<String[]> feeSchedule) {
        List<BigDecimal[]> bigDecimalList = new ArrayList<>();
        feeSchedule.forEach(fees -> {
            bigDecimalList.add(
                    Arrays.stream(fees)
                            .map(BigDecimal::new)
                            .toArray(BigDecimal[]::new)
            );
        });
        return bigDecimalList;
    }

    public List<BigDecimal[]> getFeeSchedule() {
        return feeSchedule;
    }

    public List<BigDecimal[]> getMakerTakerFees() {
        return makerTakerFees;
    }
}
