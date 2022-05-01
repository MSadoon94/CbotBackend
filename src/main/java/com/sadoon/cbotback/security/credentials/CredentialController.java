package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.update.UserUpdater;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CredentialController {
    private UserUpdater userUpdater;
    private CredentialsService credentialsService;
    private Map<Principal, List<ExchangeName>> exchangesAdded = new LinkedHashMap<>();

    public CredentialController(UserUpdater userUpdater, CredentialsService credentialsService) {
        this.userUpdater = userUpdater;
        this.credentialsService = credentialsService;
    }

    @MessageMapping("/add-credentials")
    public String addCredentials(Principal principal, SecurityCredential credentials) throws Exception {
        credentialsService.addCredentials(principal.getName(), credentials);
        ExchangeName exchangeName = ExchangeName.valueOf(credentials.type().toUpperCase());

        if (!exchangesAdded.containsKey(principal)) {
            exchangesAdded.put(principal, new ArrayList<>());
        }

        if (!exchangesAdded.get(principal).contains(exchangeName)) {
            userUpdater.addBalanceUpdates(
                    exchangeName,
                    principal,
                    Flux.interval(Duration.ofSeconds(30))
            );
            exchangesAdded.get(principal).add(exchangeName);
        }

        return String.format("%s added successfully.", exchangeName.name());
    }

}