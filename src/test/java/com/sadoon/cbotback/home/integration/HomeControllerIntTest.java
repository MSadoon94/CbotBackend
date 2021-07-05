package com.sadoon.cbotback.home.integration;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.brokerage.kraken.KrakenAccount;
import com.sadoon.cbotback.home.HomeController;
import com.sadoon.cbotback.home.HomeRepository;
import com.sadoon.cbotback.home.models.CardRequest;
import com.sadoon.cbotback.home.models.KrakenCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

@SpringBootTest
@ActiveProfiles("test")
class HomeControllerIntTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private AppProperties props;

    @Autowired
    private HomeRepository repo;

    @Autowired
    private WebClient webClient;

    private CardRequest TEST_REQUEST;

    private HomeController controller;

    private ResponseEntity<HttpStatus> response;

    private KrakenCard card;

    @BeforeEach
    void setUp() {
        TEST_REQUEST = new CardRequest(props.getKrakenApiKey(), props.getKrakenSecretKey(), "balance");
        repo.deleteAll();
        controller = new HomeController(repo, webClient);
        response = controller.addBrokerageCard(TEST_REQUEST);
        card = repo.getBrokerageCardByAccount(TEST_REQUEST.getAccount());
        card.setKrakenAccount(testAccount());
    }

    @Test
    void shouldCreateNewBrokerageCard() {
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void shouldPassRequestAccountToCard() {
        assertThat(card.getAccount(), is(TEST_REQUEST.getAccount()));
    }

    @Test
    void shouldPassRequestPasswordToCard() {
        assertThat(encoder.matches(TEST_REQUEST.getPassword(), card.getPassword()), is(true));
    }

    @Test
    void shouldAddAccountBalanceToCard() {
        assertThat(controller.getCard(TEST_REQUEST.getAccount()).getKrakenAccount(), is(samePropertyValuesAs(testAccount())));
    }

    @Test
    void shouldGetCard() {
        assertThat(controller.getCard(TEST_REQUEST.getAccount()), is(samePropertyValuesAs(card, "krakenAccount")));
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
