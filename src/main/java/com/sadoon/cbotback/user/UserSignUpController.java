package com.sadoon.cbotback.user;

import com.sadoon.cbotback.user.models.SignUpRequest;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserSignUpController {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UserRepository repo;

    public UserSignUpController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> addUser(@RequestBody SignUpRequest request){
        User user =
                new User(
                        request.getUsername(),
                        encoder.encode(request.getPassword()),
                        request.getAuthority()
                );
        repo.save(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
