package com.sadoon.cbotback.cryptoprofile;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CryptoProfileRepository extends MongoRepository<CryptoProfile, String> {

     CryptoProfile findCryptoProfileByUsername(String username);
     CryptoProfile getCryptoProfilesByUsername(String username);
}
