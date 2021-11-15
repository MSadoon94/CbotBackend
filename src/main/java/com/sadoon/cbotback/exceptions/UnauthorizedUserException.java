package com.sadoon.cbotback.exceptions;

public class UnauthorizedUserException extends CustomException{
    public UnauthorizedUserException(String message) {
        super(String.format("User is not logged in due to: %s Please login and try again.", message));
    }

    public UnauthorizedUserException(String message, ApiError subError) {
        super(String.format("User is not logged in due to: %s Please login and try again.", message), subError);
    }
}
