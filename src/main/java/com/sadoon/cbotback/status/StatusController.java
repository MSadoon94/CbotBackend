package com.sadoon.cbotback.status;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class StatusController {

    private UserService userService;

    public StatusController(UserService userService) {
        this.userService = userService;
    }

    @SubscribeMapping("/cbot-status")
    public CbotStatus getStatus(Principal principal) throws UserNotFoundException {
        return userService.getUserWithUsername(principal.getName()).getCbotStatus();
    }

    @MessageMapping("/cbot-status")
    public CbotStatus updateStatus(@Payload CbotStatus status, Principal principal) throws UserNotFoundException {
        return userService.updateStatus(userService.getUserWithUsername(principal.getName()), status).getCbotStatus();
    }
}
