package com.sadoon.cbotback.security;

import com.sadoon.cbotback.user.UserRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class CredentialsService {

    private CredentialManager manager;
    private UserRepository repo;

    public CredentialsService(CredentialManager manager, UserRepository repo) {
        this.manager = manager;
        this.repo = repo;
    }

    public void addCredentials(Principal principal, SecurityCredentials credentials)
            throws Exception {

        repo.getUserByUsername(principal.getName())
                .setCredential(
                        credentials.type(),
                        new SecurityCredentials(
                                credentials.type(),
                                credentials.account(),
                                manager.addCredentials(principal, credentials))
                );
    }

    public SecurityCredentials getCredentials(Principal principal, String type) throws Exception {
        SecurityCredentials credentials =
                repo.getUserByUsername(principal.getName()).getCredential(type);

        return new SecurityCredentials(
                type,
                credentials.account(),
                manager.decryptPassword(credentials, principal)
        );
    }

}
