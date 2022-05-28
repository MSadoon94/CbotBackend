package com.sadoon.cbotback.exchange.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Candle(
        @JsonProperty long time,
        @JsonProperty String open,
        @JsonProperty String high,
        @JsonProperty String low,
        @JsonProperty String close
) {
}