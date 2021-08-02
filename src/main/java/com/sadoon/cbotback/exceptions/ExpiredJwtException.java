package com.sadoon.cbotback.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ExpiredJwtException extends InvalidJwtException {

    private static final long serialVersionUID = 1L;

    public ExpiredJwtException(String jwt, String message) {
        super(jwt, ExpiredJwtException.class + ": " + message);
    }

}