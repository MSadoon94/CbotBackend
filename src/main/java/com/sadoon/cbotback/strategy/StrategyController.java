package com.sadoon.cbotback.strategy;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private SimpMessagingTemplate messagingTemplate;

    public StrategyController(UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/user/strategy")
    public ResponseEntity<HttpStatus> saveStrategy(@RequestBody Strategy strategy, Principal principal)
            throws UserNotFoundException {

        userService.addStrategy(userService.getUserWithUsername(principal.getName()), strategy);

        messagingTemplate.convertAndSend(
                "/topic/strategies/details",
                getStrategyDetails(principal)
        );

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

    @SubscribeMapping("/strategies/details")
    public List<Map<String, String>> getStrategyDetails(Principal principal) throws UserNotFoundException {


        return userService.getUserWithUsername(principal.getName())
                .getStrategies()
                .values()
                .stream()
                .map(strategy -> Map.of(
                        "name", strategy.getName(),
                        "isActive", String.valueOf(strategy.isActive())
                ))
                .toList();
    }

    @MessageMapping("/strategies/{name}/{status}")
    public Map<String, Boolean> addActiveStrategy(Principal principal,
                                                  @DestinationVariable String name,
                                                  @DestinationVariable("status") boolean isActive
    ) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        user.getStrategies().get(name)
                .setActive(isActive);
        userService.replace(user);
        messagingTemplate.convertAndSend(
                "/topic/strategies/details",
                getStrategyDetails(principal)
        );

        return Map.of(name, isActive);
    }
}
