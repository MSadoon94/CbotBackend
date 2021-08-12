package com.sadoon.cbotback.user;

import com.mongodb.lang.NonNull;
import com.sadoon.cbotback.user.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    private UserRepository repo;

    public MongoUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String userName) throws UsernameNotFoundException {
        User user = repo.getUserByUsername(userName);

        if (user == null) {
            throw new UsernameNotFoundException("Bad credentials");
        }

        return user;
    }
}
