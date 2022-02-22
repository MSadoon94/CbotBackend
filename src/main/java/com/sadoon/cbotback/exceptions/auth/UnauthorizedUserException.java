package com.sadoon.cbotback.exceptions.auth;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class UnauthorizedUserException extends CustomException {
    public UnauthorizedUserException(String message) {
        super(String.format("User is not logged in due to: %s Please login and try again.", message));
    }

    public UnauthorizedUserException(String message, ApiError subError) {
        super(String.format("User is not logged in due to: %s Please login and try again.", message), subError);
    }
}
