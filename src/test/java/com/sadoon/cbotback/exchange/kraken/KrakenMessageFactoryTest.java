package com.sadoon.cbotback.exchange.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class KrakenMessageFactoryTest {

    private ObjectMapper mapper = new ObjectMapper();
    private KrakenMessageFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new KrakenMessageFactory(mapper);
    }

    @Test
    void shouldReturnKrakenTickerMessageMono() throws JsonProcessingException {
        String[] mockValues = new String[]{"1000", "1", "1"};
        assertThat(
                factory.tickerMessage(
                        mapper.writeValueAsString(socketMessage(mockValues))
                ).block(),
                samePropertyValuesAs(Mocks.krakenTickerMessage(mockValues))
        );
    }

    @Test
    void shouldReturnTickerSubscribeMessageMono() throws JsonProcessingException {
        String expected = mapper.writeValueAsString(Map.of(
                "event", "subscribe",
                "pair", new String[]{"BTC/USD"},
                "subscription", Map.of("name", "ticker")
        ));

        assertThat(factory.tickerSubscribe(List.of("BTC/USD")).block(), is(equalTo(expected)));
    }

    private Object[] socketMessage(String[] mockValues) {
        return new Object[]{
                "", Map.of(
                "a", mockValues,
                "b", mockValues,
                "o", mockValues,
                "c", mockValues,
                "h", mockValues,
                "l", mockValues,
                "v", mockValues
        ), "", "BTC/USD"
        };
    }

}
