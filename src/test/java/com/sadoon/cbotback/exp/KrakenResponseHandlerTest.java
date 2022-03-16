package com.sadoon.cbotback.exp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.kraken.KrakenResponseHandler;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KrakenResponseHandlerTest {
    private ObjectMapper mapper = new ObjectMapper();

    private KrakenResponseHandler responseHandler;

    private TradeVolume volume = Mocks.mockTradeVolume(new String[]{});

    @BeforeEach
    public void setUp() {
        responseHandler = new KrakenResponseHandler(mapper);
    }

    @Test
    void shouldReturnListOfFees() throws ExchangeRequestException {
        volume.setProperties("result", Map.of(
                "fees", Map.of("BTC/USD",
                        Map.of(
                                "fee", "0.1000",
                                "minfee", "0.1000",
                                "maxfee", "0.2600",
                                "nextfee", "null",
                                "nextvolume", "null",
                                "tiervolume", "10000000.0000"
                        ))
        ));
        assertThat(responseHandler.getFees(volume).get(0), samePropertyValuesAs(Mocks.mockFees()));
    }

    @Test
    void shouldThrowExceptionWhenKrakenReturnsError(){
        volume.setErrors(new String[]{"error"});

        assertThrows(ExchangeRequestException.class, () -> responseHandler.getFees(volume));
    }
}
