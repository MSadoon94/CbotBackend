package com.sadoon.cbotback.exchange.model;

import com.fasterxml.jackson.annotation.*;
import com.sadoon.cbotback.api.KrakenResponse;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.ExchangeNames;

import java.util.*;

public class TradeVolume implements KrakenResponse {
    @JsonProperty("error")
    private String[] errors;
    private Map<String, Object> properties = new LinkedHashMap<>();

    @Override
    public String[] getErrors() {
        return Optional.ofNullable(errors).orElse(new String[]{});
    }

    @Override
    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    @JsonAnySetter
    public void setProperties(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonIgnore
    public Map<String, Object> getPropertiesByExchange(ExchangeNames name) throws ExchangeRequestException {
        checkErrors(errors);
        return (Map<String, Object>) properties.getOrDefault(name.getResponseKey(), properties);
    }

    @JsonIgnore
    public Map<String, Object> getKrakenTradeVolume() throws ExchangeRequestException {
        checkErrors(errors);
        return (Map<String, Object>) properties.get("result");
    }

}