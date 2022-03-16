package com.sadoon.cbotback.exchange;

import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.TradeVolume;

import java.util.List;

public interface ExchangeResponseHandler {

    List<Fees> getFees(TradeVolume volume) throws ExchangeRequestException;
}