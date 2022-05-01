package com.sadoon.cbotback.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.sadoon.cbotback.brokerage.model.Balances;

public record BalanceUpdate(
        @JsonProperty("exchange") String exchange,
        @JsonProperty("balances") @JsonUnwrapped Balances balances
        ) {
}