package com.sadoon.cbotback.exceptions.outofbounds;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class OutOfBoundsException extends CustomException {
    public OutOfBoundsException(String outlier, String range) {
        super(String.format("%1s is outside the range of %2s", outlier, range));
    }

    protected OutOfBoundsException(String outlier, String range,  ApiError subError) {
        super(String.format("%1s is outside the range of %2s", outlier, range), subError);
    }
}