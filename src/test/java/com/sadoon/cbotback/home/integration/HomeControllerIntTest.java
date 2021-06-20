package com.sadoon.cbotback.home.integration;

import com.sadoon.cbotback.home.HomeController;
import com.sadoon.cbotback.home.HomeRepository;
import com.sadoon.cbotback.home.models.BrokerageCard;
import com.sadoon.cbotback.home.models.CardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class HomeControllerIntTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final CardRequest TEST_REQUEST =
            new CardRequest("Account", "Password");

    @Autowired
    private HomeRepository repo;

    private HomeController controller;

    private ResponseEntity<HttpStatus> response;

    private BrokerageCard card;

    @BeforeEach
    void setUp(){
        repo.deleteAll();
        controller = new HomeController(repo);
        response = controller.addBrokerageCard(TEST_REQUEST);
        card = repo.getBrokerageCardByAccount(TEST_REQUEST.getAccount());
    }

    @Test
    void shouldCreateNewBrokerageCard(){
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void shouldPassRequestAccountToCard(){
        assertThat(card.getAccount(), is(TEST_REQUEST.getAccount()));
    }

    @Test
    void shouldPassRequestPasswordToCard(){
        assertThat(encoder.matches(TEST_REQUEST.getPassword(), card.getPassword()), is(true));
    }
}
