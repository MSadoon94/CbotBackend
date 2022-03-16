package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exceptions.notfound.EntityNotFoundException;
import com.sadoon.cbotback.exceptions.notfound.StrategyTypeNotFoundException;
import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PriceCalculatorTest {

    private PriceCalculator calculator;

    private Strategy mockStrategy = Mocks.strategy();
    private String entry = String.valueOf(Math.random());
    private String price = String.valueOf(Math.floor(Math.random() * ((1000000 - 1 + 1) + 1)));
    private String[] mockValues = new String[]{price, "1", "1"};

    @BeforeEach
    public void setUp() {
        mockStrategy.setBrokerage("mockBrokerage");
        calculator = new PriceCalculator();
    }

    @RepeatedTest(10)
    void shouldReturnTargetPriceAsBigDecimalForLongStrategies() throws OutOfBoundsException, EntityNotFoundException {
        mockStrategy.setEntry(entry);
        mockStrategy.setType(StrategyType.LONG.name());

        BigDecimal solution =
                new BigDecimal(Mocks.krakenTickerMessage(mockValues).getBid()).subtract(
                                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getBid())))
                        .subtract(new BigDecimal(Mocks.mockFees().getFee()));

        assertThat(calculator.targetPrice(
                        Mocks.krakenTickerMessage(mockValues),
                        mockStrategy,
                        Mocks.mockFees()),
                is(solution));
    }

    @RepeatedTest(10)
    void shouldReturnTargetPriceAsBigDecimalForShortStrategies() throws EntityNotFoundException, OutOfBoundsException {
        mockStrategy.setEntry(entry);
        mockStrategy.setType(StrategyType.SHORT.name());

        BigDecimal solution =
                new BigDecimal(Mocks.krakenTickerMessage(mockValues).getAsk()).add(
                                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getAsk())))
                        .add(new BigDecimal(Mocks.mockFees().getFee()));

        assertThat(calculator.targetPrice(
                        Mocks.krakenTickerMessage(mockValues),
                        mockStrategy,
                        Mocks.mockFees()),
                is(solution));
    }

    @Test
    void shouldThrowExceptionIfStrategyTypeNotFound() {
        mockStrategy.setEntry(entry);
        mockStrategy.setType("unknown");

        assertThrows(StrategyTypeNotFoundException.class, () -> calculator.targetPrice(
                Mocks.krakenTickerMessage(mockValues),
                mockStrategy,
                Mocks.mockFees()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "101"})
    void shouldThrowExceptionIfStrategyEntryIsOutOfBounds(String value) {
        mockStrategy.setEntry(value);
        mockStrategy.setType(StrategyType.LONG.name());

        assertThrows(OutOfBoundsException.class, () -> calculator.targetPrice(
                Mocks.krakenTickerMessage(mockValues),
                mockStrategy,
                Mocks.mockFees()));
    }


}
