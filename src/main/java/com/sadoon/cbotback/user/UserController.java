package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.not_found.UserNotFoundException;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<User> getUser(Principal principal) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserWithUsername(principal.getName()));
    }

}