package com.sadoon.cbotback.exchange.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sadoon.cbotback.api.KrakenResponse;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonProperty.Access;
import static java.util.Map.Entry;

public class Balances implements KrakenResponse {
    private Map<String, BigDecimal> balances;

    private String[] errors;

    public Balances() {
    }

    public Balances(Map<String, BigDecimal> balances) {
        this.balances = balances;
    }

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    @JsonProperty("result")
    public void unpackBalance(Map<String, String> result) {
        this.balances = result.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), new BigDecimal(entry.getValue())))
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public String[] getErrors() {
        return Optional.ofNullable(errors).orElse(new String[]{});
    }

    @JsonProperty(value = "error", access = Access.WRITE_ONLY)
    public void setErrors(String[] errors) {
        this.errors = errors;
    }
}
