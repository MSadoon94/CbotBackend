package com.sadoon.cbotback.exceptions.auth;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class ProcessingException extends CustomException {

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, ApiError subError) {
        super(message, subError);
    }
}