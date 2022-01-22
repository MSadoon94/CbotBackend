package com.sadoon.cbotback.exceptions;

public class BrokerageNotFoundException extends EntityNotFoundException {

    public BrokerageNotFoundException(String brokerage) {
        super(String.format("Brokerage with name '%s'", brokerage));
    }

    public BrokerageNotFoundException(String brokerage, ApiError subError) {
        super(String.format("Brokerage with name '%s'", brokerage));
        this.subError = subError;
    }
}
