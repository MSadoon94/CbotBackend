package com.sadoon.cbotback.asset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sadoon.cbotback.api.KrakenResponse;

import java.util.*;

public class AssetPairs implements KrakenResponse {

    @JsonAlias("result")
    private Map<String, AssetPair> pairs;

    private List<String> errors;

    public Map<String, AssetPair> getPairs() {
        return pairs;
    }

    public void setPairs(Map<String, AssetPair> pairs) {
        this.pairs = pairs;
    }

    @Override
    public List<String> getErrors() {
        return Optional.ofNullable(errors).orElse(Collections.emptyList());
    }

    @JsonProperty("error")
    public void unpackErrors(String[] error) {
        this.errors = Arrays.stream(error).toList();
    }
}
