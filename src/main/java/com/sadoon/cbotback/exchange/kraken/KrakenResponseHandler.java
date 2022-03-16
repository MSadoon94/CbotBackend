package com.sadoon.cbotback.exchange.kraken;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.exchange.ExchangeResponseHandler;

import java.util.ArrayList;
import java.util.List;

public class KrakenResponseHandler implements ExchangeResponseHandler {
    private ObjectMapper mapper;

    public KrakenResponseHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Fees> getFees(TradeVolume volume) throws ExchangeRequestException {
        JsonNode node = mapper.convertValue(volume.getKrakenTradeVolume().get("fees"), JsonNode.class);
        List<Fees> fees = new ArrayList<>();
        node.fields().forEachRemaining(
                entry -> fees.add(mapper.convertValue(entry.getValue(), Fees.class).setPair(entry.getKey()))
        );
        return fees;
    }
}