package com.sadoon.cbotback.home;

import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.kraken.KrakenAccount;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.home.models.CardApiRequest;
import com.sadoon.cbotback.home.models.KrakenCard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomeController {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private HomeRepository repo;

    private WebClientService client;
    private BrokerageService brokerageService;

    public HomeController(HomeRepository repo, BrokerageApiModule brokerageApiModule) {
        this.repo = repo;
        this.client = brokerageApiModule.getWebClientService();
        this.brokerageService = brokerageApiModule.getBrokerageService();
    }

    @PostMapping("/home/card")
    public ResponseEntity<HttpStatus> addBrokerageCard(@RequestBody CardApiRequest request) {

        repo.save(createCard(request, createKrakenAccount(request)));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/home/card/{account}")
    public KrakenCard getCard(@PathVariable String account) {
        return repo.getBrokerageCardByAccount(account);
    }

    private KrakenAccount createKrakenAccount(CardApiRequest request) {
        KrakenAccount account = new KrakenAccount();
        account.setAccountBalance(client.onResponse(brokerageService.createBrokerageDto(request, "balance")));
        return account;
    }

    private KrakenCard createCard(CardApiRequest request, KrakenAccount account) {
        KrakenCard card
                = new KrakenCard(request.getAccount(), encoder.encode(request.getPassword()));
        card.setKrakenAccount(account);
        return card;
    }


}
