package com.sadoon.cbotback.util;

import com.sadoon.cbotback.exceptions.duplication.DuplicateUserException;
import com.sadoon.cbotback.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DuplicateHandler {
    private UserRepository repo;

    public DuplicateHandler(UserRepository repo) {
        this.repo = repo;
    }

    public void checkForExistingUser(String username) throws DuplicateUserException {
        if(repo.getUserByUsername(username) != null){
            throw new DuplicateUserException(
                    "user",
                    String.format("with the username '%s', please choose another username.", username));
        }
    }

}
