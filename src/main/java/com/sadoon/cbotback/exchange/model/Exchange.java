package com.sadoon.cbotback.exchange.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record Exchange(
        @JsonProperty("exchange") String exchange,
        @JsonProperty("balances") @JsonUnwrapped Balances balances
        ) {
}