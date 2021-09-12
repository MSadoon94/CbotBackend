package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyController;
import com.sadoon.cbotback.strategy.StrategyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

@SpringBootTest
class StrategyControllerIntTest {

    private Strategy testStrategy;

    @Autowired
    private StrategyRepository repo;

    private StrategyController controller;

    @BeforeEach
    public void setUp() {
        repo.deleteAll();
        setStrategy();
        controller = new StrategyController(repo);
    }

    @Test
    void shouldReturnHttpCreatedStatusForSuccessfulSaveRequests() {
        controller.saveStrategy(testStrategy);
        assertThat(repo.getStrategyByName(testStrategy.getName()), is(samePropertyValuesAs(testStrategy)));
    }

    private void setStrategy() {
        testStrategy = new Strategy();
        testStrategy.setName("TestStrategy");
    }
}
