package com.sadoon.cbotback.security;

public record SecurityCredentials (
        String type,
        String account,
        String password
){
}
