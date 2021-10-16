package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.brokerage.BrokerageRepository;
import com.sadoon.cbotback.brokerage.kraken.KrakenAccount;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.home.HomeController;
import com.sadoon.cbotback.home.HomeRepository;
import com.sadoon.cbotback.home.models.CardApiRequest;
import com.sadoon.cbotback.home.models.KrakenCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

@SpringBootTest
class HomeControllerIntTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private AppProperties props;

    @Autowired
    private HomeRepository repo;

    @Autowired
    private BrokerageRepository brokerageRepo;

    private CardApiRequest request;

    private HomeController controller;

    private ResponseEntity<HttpStatus> response;

    private KrakenCard card;

    @BeforeEach
    void setUp() {
        request = Mocks.cardRequest("kraken");
        repo.deleteAll();
        controller = new HomeController(repo, new BrokerageApiModule(brokerageRepo));
        response = controller.addBrokerageCard(request);
        card = repo.getBrokerageCardByAccount(request.getAccount());
        card.setKrakenAccount(testAccount());
    }

    @Test
    void shouldCreateNewBrokerageCard() {
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void shouldPassRequestAccountToCard() {
        assertThat(card.getAccount(), is(request.getAccount()));
    }

    @Test
    void shouldPassRequestPasswordToCard() {
        assertThat(encoder.matches(request.getPassword(), card.getPassword()), is(true));
    }

    @Test
    void shouldAddAccountBalanceToCard() {
        assertThat(controller.getCard(request.getAccount()).getKrakenAccount(), is(samePropertyValuesAs(testAccount())));
    }

    @Test
    void shouldGetCard() {
        assertThat(controller.getCard(request.getAccount()), is(samePropertyValuesAs(card, "krakenAccount")));
    }

    private KrakenAccount testAccount() {
        KrakenAccount account = new KrakenAccount();

        Map<String, String> balance = new LinkedHashMap<>();

        //Due to a bug in Kraken's API, accounts with balances of zero will return empty error messages when requested.
        balance.put("error", "");

        account.setAccountBalance(balance);

        return account;
    }
}
