package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.duplication.DuplicateUserException;
import com.sadoon.cbotback.user.models.SignUpRequest;
import com.sadoon.cbotback.user.models.User;
import com.sadoon.cbotback.util.DuplicateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignUpController {

    @Autowired
    private PasswordEncoder encoder;

    private UserService userService;
    private DuplicateHandler duplicateHandler;

    public SignUpController(PasswordEncoder encoder, UserService userService, DuplicateHandler duplicateHandler) {
        this.encoder = encoder;
        this.userService = userService;
        this.duplicateHandler = duplicateHandler;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<HttpStatus> addUser(@RequestBody SignUpRequest request) throws DuplicateUserException {
        duplicateHandler.checkForExistingUser(request.getUsername());
        User user =
                new User(
                        request.getUsername(),
                        encoder.encode(request.getPassword()),
                        new SimpleGrantedAuthority(request.getAuthority())
                );
        userService.save(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
