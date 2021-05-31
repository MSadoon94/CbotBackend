package com.sadoon.cbotback.security;

import com.sadoon.cbotback.security.jwt.JwtUtil;
import com.sadoon.cbotback.security.models.AuthenticationRequest;
import com.sadoon.cbotback.security.models.AuthenticationResponse;
import com.sadoon.cbotback.security.services.MongoUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
public class SecurityController {

    private AuthenticationManager manager;

    private MongoUserDetailsService userDetailsService;

    private JwtUtil jwtUtil;

    public SecurityController(AuthenticationManager manager, MongoUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.manager = manager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken
            (@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

        try {
            manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        return ResponseEntity.ok(new AuthenticationResponse(getJwt(authenticationRequest)));
    }

    private String getJwt(AuthenticationRequest request){
        return jwtUtil.generateToken(
                userDetailsService.loadUserByUsername(request.getUsername()));
    }
}
