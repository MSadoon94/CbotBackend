package com.sadoon.cbotback.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CbotStatus(
        @JsonProperty("isActive") boolean isActive,
        @JsonProperty("activeStrategies") List<String> activeStrategies
) {

}