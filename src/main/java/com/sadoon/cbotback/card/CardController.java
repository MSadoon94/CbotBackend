package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.home.models.CardApiRequest;
import com.sadoon.cbotback.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

@RestController
public class CardController {

    private UserService userService;
    private WebClientService webClientService;

    private BrokerageService brokerageService;

    public CardController(UserService userService, BrokerageApiModule apiModule) {
        this.userService = userService;
        this.webClientService = apiModule.getWebClientService();
        this.brokerageService = apiModule.getBrokerageService();
    }

    @GetMapping("/load-cards")
    public ResponseEntity<List<Card>> loadCards(Principal principal) throws UserNotFoundException {
        List<Card> cards = userService.getUser(principal.getName()).getCards();
        return ResponseEntity.ok().body(cards);
    }

    @PostMapping("/save-card")
    public ResponseEntity<String> saveCard(@RequestBody CardApiRequest request, Principal principal) throws UserNotFoundException {
        userService.addCard(userService.getUser(principal.getName()), card(request));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private Card card(CardApiRequest request) {
        Card card = new Card();
        card.setCardName(request.getCardName());
        card.setBalances(validateAccount(request));
        return card;
    }

    private Map<String, BigDecimal> validateAccount(CardApiRequest request) {

        Map<String, String> response =
                webClientService.onResponse(brokerageService.createBrokerageDto(request, "balance"));

        return response.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> new BigDecimal(entry.getValue())
                ));

    }

}
