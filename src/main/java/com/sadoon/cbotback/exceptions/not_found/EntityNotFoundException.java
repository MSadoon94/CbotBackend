package com.sadoon.cbotback.exceptions.not_found;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class EntityNotFoundException extends CustomException {

    public EntityNotFoundException(String message) {
        super(String.format("%s was not found.", message));
    }

    public EntityNotFoundException(String message, ApiError subError) {
        super(String.format("%s was not found.", message), subError);
        this.subError = subError;
    }
}
