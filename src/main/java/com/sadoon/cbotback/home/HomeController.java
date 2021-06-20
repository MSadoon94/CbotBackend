package com.sadoon.cbotback.home;

import com.sadoon.cbotback.home.models.BrokerageCard;
import com.sadoon.cbotback.home.models.CardRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    private HomeRepository repo;

    public HomeController(HomeRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/home/card")
    public ResponseEntity<HttpStatus> addBrokerageCard(@RequestBody CardRequest request){
        BrokerageCard card
                = new BrokerageCard(request.getAccount(), encoder.encode(request.getPassword()));

        repo.save(card);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
