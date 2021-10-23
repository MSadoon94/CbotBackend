package com.sadoon.cbotback.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.InvalidKeyException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@JsonTest
class CardControllerTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private BrokerageService brokerageService;
    @Mock
    private UserService userService;
    @Mock
    private CardService cardService;
    @Mock
    private WebClientService webClientService;

    @Mock
    private BrokerageApiModule apiModule;

    @InjectMocks
    private CardController cardController;

    private User mockUser = Mocks.user();
    private Balances balances = Mocks.balances("USD", "100");

    private final Authentication auth = Mocks.auth(mockUser);

    private final List<Card> cards = Mocks.cardList();

    private CardApiRequest cardRequest = Mocks.cardRequest("kraken");

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockUser.setCards(cards);
        userService.deleteAll();
        userService.save(mockUser);
    }

    @Test
    void shouldReturnCardNamesConnectedToAccountOnLoadCardsSuccess() throws Exception {
        given(userService.getUser(auth.getName()))
                .willReturn(mockUser);

        loadCards()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[1].cardName", is(cards.get(1).getCardName())));
    }

    @Test
    void shouldReturnCreatedStatusOnSaveCardSuccess() throws Exception {
        given(userService.getUser(auth.getName())).willReturn(mockUser);
        given(webClientService.onResponse(any(), any())).willReturn(balances);
        given(cardService.newCard(any())).willReturn(Mocks.card());

        cardRequest.setCardName("mockCard3");
        cardRequest.setBrokerage("kraken");

        saveCard()
                .andExpect(status().isCreated());
    }


    @Test
    void shouldReturnNotFoundWhenLoadCardsFailToFindUser() throws Exception {
        UserNotFoundException exception = new UserNotFoundException(auth.getName());
        given(userService.getUser(auth.getName()))
                .willThrow(exception);

        loadCards()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));

    }

    @Test
    void shouldReturnApiErrorWhenSaveCardFailsToFindUser() throws Exception {
        UserNotFoundException exception = new UserNotFoundException(auth.getName());
        given(userService.getUser(auth.getName()))
                .willThrow(exception);

        saveCard()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    @Test
    void shouldReturnApiErrorWhenSaveCardFailsDueToEncryptionError() throws Exception {
        InvalidKeyException exception = new InvalidKeyException("InvalidKeyException");
        given(userService.getUser(auth.getName())).willReturn(mockUser);
        given(cardService.newCard(any())).willReturn(Mocks.card());
        given(webClientService.onResponse(any(), any())).willReturn(balances);
        given(cardService.encryptCard(any())).willThrow(exception);

        saveCard()
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message",
                        is(String.format("Error while encrypting password: %s", exception.getMessage()))));
    }

    private ResultActions loadCards() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get("/load-cards")
                .principal(auth));
    }

    private ResultActions saveCard() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/save-card")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest))
                .principal(auth));
    }
}

