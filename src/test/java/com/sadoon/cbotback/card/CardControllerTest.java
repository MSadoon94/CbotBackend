package com.sadoon.cbotback.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.card.models.CardPasswordVerificationRequest;
import com.sadoon.cbotback.exceptions.notfound.BrokerageNotFoundException;
import com.sadoon.cbotback.exceptions.notfound.CardNotFoundException;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exceptions.password.PasswordException;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.*;
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
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
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

    private final Map<String, Card> cards = Mocks.cards();
    private final String mockCardName = "mockCard1";

    private CardApiRequest cardRequest = Mocks.cardRequest("kraken");
    private CardPasswordVerificationRequest passwordVerification =
            new CardPasswordVerificationRequest("mockName", "mockPassword");

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
        given(userService.getUserWithUsername(auth.getName()))
                .willReturn(mockUser);

        loadCards()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mockCard1", is(notNullValue())))
                .andExpect(jsonPath("$.mockCard2", is(notNullValue())));
    }

    @Test
    void shouldReturnCreatedStatusOnSaveCardSuccess() throws Exception {
        given(userService.getUserWithUsername(auth.getName())).willReturn(mockUser);
        given(webClientService.onResponse(any(), any())).willReturn(balances);
        given(cardService.newCard(any())).willReturn(Mocks.card());

        cardRequest.setCardName("mockCard3");
        cardRequest.setExchange("kraken");

        saveCard()
                .andExpect(status().isCreated());
    }


    @Test
    void shouldReturnNotFoundWhenLoadCardsFailToFindUser() throws Exception {
        UserNotFoundException exception = new UserNotFoundException(auth.getName());
        given(userService.getUserWithUsername(auth.getName()))
                .willThrow(exception);

        loadCards()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));

    }

    @Test
    void shouldReturnApiErrorWhenSaveCardFailsToFindUser() throws Exception {
        UserNotFoundException exception = new UserNotFoundException(auth.getName());
        given(userService.getUserWithUsername(auth.getName()))
                .willThrow(exception);

        saveCard()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    @Test
    void shouldReturnApiErrorWhenSaveCardFailsDueToEncryptionError() throws Exception {
        InvalidKeyException exception = new InvalidKeyException("InvalidKeyException");
        given(userService.getUserWithUsername(auth.getName())).willReturn(mockUser);
        given(cardService.newCard(any())).willReturn(Mocks.card());
        given(webClientService.onResponse(any(), any())).willReturn(balances);
        given(cardService.encryptCard(any(), any())).willThrow(exception);

        saveCard()
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message",
                        is(String.format("Error while encrypting password: %s", exception.getMessage()))));
    }

    @Test
    void shouldThrowNotFoundOnSaveCardRequestWithUnknownBrokerage() throws Exception {
        BrokerageNotFoundException exception = new BrokerageNotFoundException(mockCardName);
        given(brokerageService.createBrokerageDto(any(), any())).willThrow(exception);
        given(userService.getUserWithUsername(auth.getName())).willReturn(mockUser);
        given(cardService.newCard(any())).willReturn(Mocks.card());

        saveCard()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    @Test
    void shouldReturnOkStatusWhenPasswordMatchesStoredPassword() throws Exception {

        cardPasswordPost()
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordDoesNotMatchStoredPassword() throws Exception {
        doThrow(new PasswordException("card password")).when(cardService).verifyPassword(any(), any(), any());
        cardPasswordPost()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCardOnSuccessfulLoadSingleCardRequest() throws Exception {
        given(cardService.getCard(any(), any())).willReturn(cards.get(mockCardName));

        loadSingleCard()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardName", is(mockCardName)));
    }

    @Test
    void shouldThrowNotFoundOnLoadSingleCardRequestWithUnknownCard() throws Exception {
        CardNotFoundException exception = new CardNotFoundException(mockCardName);
        given(cardService.getCard(any(), any())).willThrow(exception);

        loadSingleCard()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    @Test
    void shouldReturnUnauthorizedOnKeyStoreException() throws Exception {
        Exception exception = new PasswordException("card password");
        doThrow(exception).when(cardService).verifyPassword(any(), any(), any());

        cardPasswordPost()
                .andExpect(status().isUnauthorized());
    }


    private ResultActions loadCards() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get("/user/cards")
                .principal(auth));
    }

    private ResultActions loadSingleCard() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get(String.format("/user/card/%s", mockCardName))
                .principal(auth));
    }

    private ResultActions saveCard() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/user/card")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest))
                .principal(auth));
    }

    private ResultActions cardPasswordPost() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/user/card-password")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordVerification))
                .principal(auth)
        );
    }
}