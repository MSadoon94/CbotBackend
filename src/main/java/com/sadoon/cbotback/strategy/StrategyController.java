package com.sadoon.cbotback.strategy;

import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
public class StrategyController {

    private UserService userService;

    public StrategyController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/save-strategy")
    public ResponseEntity<HttpStatus> saveStrategy(@RequestBody Strategy strategy, Principal principal)
            throws UserNotFoundException {

        userService.addStrategy(userService.getUserWithUsername(principal.getName()), strategy);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/load-strategies")
    public ResponseEntity<Map<String, Strategy>> loadStrategies(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        return ResponseEntity.ok(user.getStrategies());
    }

    @GetMapping("/load-strategy/{strategy}")
    public ResponseEntity<Strategy> loadStrategy(@PathVariable("strategy") String strategy, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        return ResponseEntity.ok(user.getStrategies().get(strategy));
    }

}
