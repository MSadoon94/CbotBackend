package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.card.models.CardPasswordVerificationRequest;
import com.sadoon.cbotback.exceptions.*;
import com.sadoon.cbotback.user.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Map;

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
    public ResponseEntity<Map<String, Card>> loadCards(Principal principal) throws UserNotFoundException {
        Map<String, Card> cards = userService.getUserWithUsername(principal.getName()).getCards();
        return ResponseEntity.ok().body(cards);
    }

    @PostMapping("/save-card")
    public ResponseEntity<String> saveCard(@RequestBody CardApiRequest request, Principal principal)
            throws EntityNotFoundException, CardPasswordEncryptionException, KrakenRequestException {

        userService.addCard(userService.getUserWithUsername(principal.getName()), createCard(request, principal));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/card-password")
    public ResponseEntity<String> verifyCardPassword(
            @RequestBody CardPasswordVerificationRequest request, Principal principal)
            throws EntityNotFoundException, PasswordException, GeneralSecurityException {

        Card card = cardService.getCard(userService.getUserWithUsername(principal.getName()), request.getCardName());
        cardService.verifyPassword(card, request.getPassword(), principal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/load-a-card/{cardName}")
    public ResponseEntity<Card> loadSingleCard(@PathVariable("cardName") String cardName, Principal principal) throws EntityNotFoundException {

        return ResponseEntity.ok(
                cardService.getCard(userService.getUserWithUsername(principal.getName()), cardName)
        );
    }

    private Card createCard(CardApiRequest request, Principal principal)
            throws CardPasswordEncryptionException, BrokerageNotFoundException, KrakenRequestException {
        try {
            Card card = cardService.newCard(request);
            card.setBalances(getBalances(request));
            return cardService.encryptCard(card, principal);
        } catch (GeneralSecurityException | IOException e) {
            throw new CardPasswordEncryptionException(e.getMessage());
        }
    }

    private Balances getBalances(CardApiRequest request) throws KrakenRequestException, BrokerageNotFoundException {
        return webClientService.onResponse(
                new ParameterizedTypeReference<Balances>() {
                },
                brokerageService.createBrokerageDto(request, "balance"));
    }
}
