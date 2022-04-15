package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.stereotype.Service;

@Service
public class CredentialsService {

    private CredentialManager manager;
    private UserService userService;

    public CredentialsService(CredentialManager manager, UserService userService) {
        this.manager = manager;
        this.userService = userService;
    }

    public void addCredentials(String username, SecurityCredential credential)
            throws Exception {
        User user = userService.getUserWithUsername(username);
        userService.cacheCredential(user, credential);
        userService.addEncryptedCredential(user, new SecurityCredential(
                credential.type(),
                credential.account(),
                manager.addCredentials(username, credential)));
    }

    public SecurityCredential getDecryptedCredentials(String username, String type) throws Exception {
        SecurityCredential credentials =
                userService.getUserWithUsername(username).getEncryptedCredential(type);

        SecurityCredential returnedCredentials = new SecurityCredential(
                type,
                credentials.account(),
                manager.decryptPassword(credentials, username));

        return returnedCredentials;
    }

}
