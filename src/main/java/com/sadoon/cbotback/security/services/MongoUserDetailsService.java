package com.sadoon.cbotback.security.services;

import com.sadoon.cbotback.cryptoprofile.CryptoProfile;
import com.sadoon.cbotback.cryptoprofile.CryptoProfileRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    private CryptoProfileRepository repo;

    public MongoUserDetailsService(CryptoProfileRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        CryptoProfile profile = repo.getCryptoProfilesByUsername(userName);

        if(profile == null){
            throw new UsernameNotFoundException("Bad credentials");
        }

        return profile;
    }
}