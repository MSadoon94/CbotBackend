package com.sadoon.cbotback.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieRemover {

    public static HttpHeaders getNullHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, getNullCookie("refresh_token", "/refresh-jwt").toString());
        headers.add(HttpHeaders.SET_COOKIE, getNullCookie("refresh_token", "/log-out").toString());
        headers.add(HttpHeaders.SET_COOKIE, getNullCookie("jwt", "/").toString());
        return headers;
    }
    private static ResponseCookie getNullCookie(String name, String path){
        return ResponseCookie
                .from(name, null)
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("%s", path))
                .maxAge(-1)
                .build();
    }
}