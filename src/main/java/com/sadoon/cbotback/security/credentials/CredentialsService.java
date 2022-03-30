package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class CredentialsService {

    private CredentialManager manager;
    private UserService userService;

    public CredentialsService(CredentialManager manager, UserService userService) {
        this.manager = manager;
        this.userService = userService;
    }

    public void addCredentials(String username, SecurityCredentials credentials)
            throws Exception {

        userService.getUserWithUsername(username)
                .setCredential(
                        credentials.type(),
                        new SecurityCredentials(
                                credentials.type(),
                                credentials.account(),
                                manager.addCredentials(username, credentials))
                );
    }

    public SecurityCredentials getCredentials(String username, String type) throws Exception {
        SecurityCredentials credentials =
                userService.getUserWithUsername(username).getCredential(type);

        return new SecurityCredentials(
                type,
                credentials.account(),
                manager.decryptPassword(credentials, username)
        );
    }

}
