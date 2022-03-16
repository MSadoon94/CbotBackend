package com.sadoon.cbotback.asset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sadoon.cbotback.api.KrakenResponse;

import java.util.Map;
import java.util.Optional;

public class AssetPairs implements KrakenResponse {

    @JsonAlias("result")
    private Map<String, AssetPair> pairs;

    private String[] errors;

    public Map<String, AssetPair> getPairs() {
        return pairs;
    }

    public void setPairs(Map<String, AssetPair> pairs) {
        this.pairs = pairs;
    }

    @Override
    public String[] getErrors() {
        return Optional.ofNullable(errors).orElse(new String[]{});
    }

    @JsonProperty("error")
    public void setErrors(String[] errors) {
        this.errors = errors;
    }
}
