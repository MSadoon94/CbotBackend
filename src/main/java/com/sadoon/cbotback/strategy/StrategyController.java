package com.sadoon.cbotback.strategy;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class StrategyController {

    private UserService userService;

    public StrategyController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/strategy")
    public ResponseEntity<HttpStatus> saveStrategy(@RequestBody Strategy strategy, Principal principal)
            throws UserNotFoundException {

        userService.addStrategy(userService.getUserWithUsername(principal.getName()), strategy);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/user/strategies")
    public ResponseEntity<Map<String, Strategy>> loadStrategies(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        return ResponseEntity.ok(user.getStrategies());
    }

    @GetMapping("/user/strategy/{strategy}")
    public ResponseEntity<Strategy> loadStrategy(@PathVariable("strategy") String strategy, Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        return ResponseEntity.ok(user.getStrategies().get(strategy));
    }

    @SubscribeMapping("/strategies/names")
    public List<String> getStrategyNames(Principal principal) throws UserNotFoundException {

        return userService.getUserWithUsername(principal.getName())
                .getStrategies()
                .keySet()
                .stream()
                .toList();
    }

    @MessageMapping("/strategies/active")
    public String addActiveStrategy(Principal principal, String name) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        user.addActiveStrategy(name);
        userService.replace(user);
        return name;
    }


}
