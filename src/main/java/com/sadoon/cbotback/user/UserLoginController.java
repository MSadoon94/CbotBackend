package com.sadoon.cbotback.user;

import com.sadoon.cbotback.security.jwt.JwtUtil;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginController.class);

    private AuthenticationManager manager;
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;

    public UserLoginController(
            AuthenticationManager manager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        this.manager = manager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest login) {

        try {
            manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getUsername(), login.getPassword())
            );
        } catch (BadCredentialsException e) {
            logger.info("Incorrect username or password", e);
        }

        return ResponseEntity.ok(new LoginResponse(getJwt(login)));
    }

    private String getJwt(LoginRequest request){
        return jwtUtil.generateToken(
                userDetailsService.loadUserByUsername(request.getUsername()));
    }

}
