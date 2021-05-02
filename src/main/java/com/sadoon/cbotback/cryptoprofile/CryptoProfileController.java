package com.sadoon.cbotback.cryptoprofile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cryptoProfile")
public class CryptoProfileController {

    private CryptoProfileRepository repository;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public CryptoProfile add(@RequestBody CryptoProfile cryptoProfile){
        return repository.save(cryptoProfile);
    }
}
