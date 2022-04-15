package com.sadoon.cbotback.security.credentials;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SecurityCredential(
        @JsonProperty("type") @JsonAlias("exchange") String type,
        @JsonProperty("account") String account,
        @JsonProperty("password") String password
){
}
