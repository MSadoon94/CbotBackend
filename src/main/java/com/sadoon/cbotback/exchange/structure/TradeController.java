package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class TradeController {
    private SimpMessagingTemplate messagingTemplate;
    private UserService userService;

    public TradeController(SimpMessagingTemplate messagingTemplate,
                           UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/trade-feed")
    public void getTradeFeed(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        userService.getTradeFeeds(userService.getUserWithUsername(principal.getName()))
                .log()
                .thenMany(userService.getTradeFeed(userService.getUserWithUsername(principal.getName()))
                )
                .subscribe(trade -> {

                    userService.addTrade(user, trade);

                    messagingTemplate.convertAndSend(
                            "/topic/trade-feed",
                            trade
                    );

                });

    }


}
