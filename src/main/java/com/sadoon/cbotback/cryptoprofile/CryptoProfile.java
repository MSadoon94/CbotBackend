package com.sadoon.cbotback.cryptoprofile;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
public class CryptoProfile {

    @Id
    private String id = UUID.randomUUID().toString();

    private final String name;

    private final String pass;

    public CryptoProfile(String name, String pass){
        this.name = name;
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public String getId() {
        return id;
    }

}
