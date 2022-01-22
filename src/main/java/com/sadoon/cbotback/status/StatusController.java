package com.sadoon.cbotback.status;

import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class StatusController {

    private UserService userService;

    public StatusController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/user/cbot-status")
    public ResponseEntity<String> updateCbotStatus(@RequestBody CbotStatus status, Principal principal)
            throws UserNotFoundException {

        userService.updateStatus(userService.getUserWithUsername(principal.getName()), status);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/cbot-status")
    public ResponseEntity<CbotStatus> getCbotStatus(Principal principal) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserWithUsername(principal.getName()).getCbotStatus());
    }

}
