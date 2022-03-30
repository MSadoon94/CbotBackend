package com.sadoon.cbotback.security.credentials;

public record SecurityCredentials (
        String type,
        String account,
        String password
){
}
