package com.sadoon.cbotback.strategy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StrategyController {

    private StrategyRepository repo;

    public StrategyController(StrategyRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/save-strategy")
    public ResponseEntity<HttpStatus> saveStrategy(@RequestBody Strategy strategy) {
        repo.save(strategy);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
