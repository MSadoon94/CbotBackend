package com.sadoon.cbotback.home;

import com.sadoon.cbotback.brokerage.BrokerageRestService;
import com.sadoon.cbotback.brokerage.kraken.KrakenAccount;
import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import com.sadoon.cbotback.home.models.CardRequest;
import com.sadoon.cbotback.home.models.KrakenCard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class HomeController {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private HomeRepository repo;

    private WebClient client;

    public HomeController(HomeRepository repo, WebClient webClient) {
        this.repo = repo;
        this.client = webClient;
    }

    @PostMapping("/home/card")
    public ResponseEntity<HttpStatus> addBrokerageCard(@RequestBody CardRequest cardRequest) {
        KrakenRequest request = new KrakenRequest(cardRequest.getAccount(), cardRequest.getPassword());

        repo.save(createCard(request, createKrakenAccount(request)));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/home/card/{account}")
    public KrakenCard getCard(@PathVariable String account) {
        return repo.getBrokerageCardByAccount(account);
    }

    private KrakenAccount createKrakenAccount(KrakenRequest request) {
        BrokerageRestService service = new BrokerageRestService(client);
        KrakenAccount account = new KrakenAccount();
        account.setAccountBalance(service.getBalance(request));
        return account;
    }

    private KrakenCard createCard(KrakenRequest request, KrakenAccount account) {
        KrakenCard card
                = new KrakenCard(request.getAccount(), encoder.encode(request.getPassword()));
        card.setKrakenAccount(account);
        return card;
    }


}