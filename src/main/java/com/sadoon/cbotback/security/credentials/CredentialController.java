package com.sadoon.cbotback.security.credentials;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class CredentialController {
    private CredentialsService credentialsService;

    public CredentialController(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @MessageMapping("/add-credentials")
    public String addCredentials(Principal principal, SecurityCredential credentials) throws Exception {
        credentialsService.addCredentials(principal.getName(), credentials);
        return "Exchange added.";
    }

}