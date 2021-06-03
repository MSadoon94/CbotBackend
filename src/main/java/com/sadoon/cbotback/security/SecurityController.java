package com.sadoon.cbotback.security;

import com.sadoon.cbotback.cryptoprofile.CryptoProfile;
import com.sadoon.cbotback.cryptoprofile.CryptoProfileRepository;
import com.sadoon.cbotback.security.jwt.JwtUtil;
import com.sadoon.cbotback.security.models.LoginRequest;
import com.sadoon.cbotback.security.models.AuthenticationResponse;
import com.sadoon.cbotback.security.models.RegistrationRequest;
import com.sadoon.cbotback.security.services.MongoUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authenticate")
public class SecurityController {

    private AuthenticationManager manager;

    private MongoUserDetailsService userDetailsService;

    private JwtUtil jwtUtil;

    private CryptoProfileRepository repository;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    public SecurityController(
            AuthenticationManager manager,
            MongoUserDetailsService userDetailsService,
            JwtUtil jwtUtil, CryptoProfileRepository repository) {
        this.manager = manager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.repository = repository;
    }

    @PostMapping("/signup")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<?> addProfile(@RequestBody RegistrationRequest request){
        CryptoProfile profile =
                new CryptoProfile(
                        request.getUsername(),
                        encoder.encode(request.getPassword()),
                        request.getAuthority()
                        );
        return ResponseEntity.ok(repository.save(profile));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken
            (@RequestBody LoginRequest login) throws Exception {

        try {
            manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getUsername(), login.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        return ResponseEntity.ok(new AuthenticationResponse(getJwt(login)));
    }

    private String getJwt(LoginRequest request){
        return jwtUtil.generateToken(
                userDetailsService.loadUserByUsername(request.getUsername()));
    }
}
