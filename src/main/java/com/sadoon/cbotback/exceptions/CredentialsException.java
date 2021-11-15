package com.sadoon.cbotback.exceptions;

public class CredentialsException extends CustomException{

    protected CredentialsException(String message) {
        super(String.format("%s is incorrect. Please try again.", message));
    }

    protected CredentialsException(String message, ApiError subError) {
        super(String.format("%s is incorrect. Please try again.", message), subError);
    }
}