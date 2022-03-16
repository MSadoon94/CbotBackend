package com.sadoon.cbotback.exp;

import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exceptions.notfound.EntityNotFoundException;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exchange.ExchangeResponseHandler;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.executor.FeeProcessor;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FeeProcessorTest {

    private Exchange exchange;
    @Mock private ExchangeResponseHandler responseHandler;
    @Mock private KrakenWebClient webClient;

    private FeeProcessor processor;

    @BeforeEach
    public void setUp(){
        exchange = new Exchange()
                .setResponseHandler(responseHandler)
                .setWebClient(webClient);
        processor = new FeeProcessor(exchange);
    }

    @Test
    void shouldReturnFees() throws ExchangeRequestException, EntityNotFoundException {
        given(responseHandler.getFees(any())).willReturn(List.of(Mocks.mockFees()));
        given(webClient.getTradeVolume(any(), any()))
                .willReturn(Mono.just(Mocks.mockTradeVolume(new String[]{})));

        assertThat(processor.fees(Mocks.assetPair()), samePropertyValuesAs(Mocks.mockFees()));
    }

    @Test
    void shouldThrowExceptionWhenPairIsNotFoundInExchangeResponse() throws ExchangeRequestException {
        Fees fees = Mocks.mockFees();
        fees.setPair("UnknownPair");
        given(responseHandler.getFees(any())).willReturn(List.of(fees));
        given(webClient.getTradeVolume(any(), any()))
                .willReturn(Mono.just(Mocks.mockTradeVolume(new String[]{})));

        assertThrows(EntityNotFoundException.class, () -> processor.fees(Mocks.assetPair()));
    }

}