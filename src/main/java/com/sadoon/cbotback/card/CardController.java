package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.exceptions.CardPasswordEncryptionException;
import com.sadoon.cbotback.exceptions.KrakenRequestException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class CardController {

    private UserService userService;
    private CardService cardService;
    private WebClientService webClientService;

    private BrokerageService brokerageService;

    public CardController(UserService userService, CardService cardService, BrokerageApiModule apiModule) {
        this.userService = userService;
        this.cardService = cardService;
        this.webClientService = apiModule.getWebClientService();
        this.brokerageService = apiModule.getBrokerageService();
    }

    @GetMapping("/load-cards")
    public ResponseEntity<List<Card>> loadCards(Principal principal) throws UserNotFoundException {
        List<Card> cards = userService.getUser(principal.getName()).getCards();
        return ResponseEntity.ok().body(cards);
    }

    @PostMapping("/save-card")
    public ResponseEntity<String> saveCard(@RequestBody CardApiRequest request, Principal principal)
            throws UserNotFoundException, CardPasswordEncryptionException {

        userService.addCard(userService.getUser(principal.getName()), getCard(request));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private Card getCard(CardApiRequest request) throws CardPasswordEncryptionException {
        try {
            Card card = cardService.newCard(request);
            card.setBalances(getBalances(request));
            card = cardService.encryptCard(card);
            return card;
        } catch (Exception e) {
            throw new CardPasswordEncryptionException(e.getMessage());
        }
    }

    private Balances getBalances(CardApiRequest request) throws KrakenRequestException {
        return webClientService.onResponse(
                new ParameterizedTypeReference<Balances>() {
                },
                brokerageService.createBrokerageDto(request, "balance"));
    }

}
