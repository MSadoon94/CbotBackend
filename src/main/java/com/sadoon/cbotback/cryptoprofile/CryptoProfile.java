package com.sadoon.cbotback.cryptoprofile;

import org.springframework.data.annotation.Id;
import java.util.UUID;


public class CryptoProfile {
    @Id
    private final String id = UUID.randomUUID().toString();

    private final String name;

    private final String pass;

    public CryptoProfile(String name, String pass){
        this.name = name;
        this.pass = pass;
    }

}
