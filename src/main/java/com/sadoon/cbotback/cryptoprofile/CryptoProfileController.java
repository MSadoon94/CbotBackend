package com.sadoon.cbotback.cryptoprofile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cryptoProfile")
public class CryptoProfileController {

    private CryptoProfileRepository repository;

    public CryptoProfileController(CryptoProfileRepository repository){
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public CryptoProfile addProfile(@RequestBody CryptoProfile cryptoProfile){
        System.out.println(cryptoProfile.getId() + " " + cryptoProfile.getUsername() + " " + cryptoProfile.getPassword());
        return repository.save(cryptoProfile);
    }

    @GetMapping("/name/{username}")
    public CryptoProfile getProfile(@PathVariable String username){
        return repository.findCryptoProfileByUsername(username);
    }

}
