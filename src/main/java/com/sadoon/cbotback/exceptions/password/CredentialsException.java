package com.sadoon.cbotback.exceptions.password;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class CredentialsException extends CustomException {

    protected CredentialsException(String message) {
        super(String.format("%s is incorrect. Please try again.", message));
    }

    protected CredentialsException(String message, ApiError subError) {
        super(String.format("%s is incorrect. Please try again.", message), subError);
    }
}