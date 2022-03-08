package com.sadoon.cbotback.exceptions.notfound;

import com.sadoon.cbotback.exceptions.ApiError;

public class StrategyTypeNotFoundException extends EntityNotFoundException {
    public StrategyTypeNotFoundException(String type) {
        super(type);
    }

    public StrategyTypeNotFoundException(String type, ApiError subError) {
        super(type, subError);
    }
}