package com.sadoon.cbotback.user;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
class UserServiceTest {

    @Autowired
    private UserRepository repo;

    private UserService userService;

    private User mockUser = Mocks.user();

    private RefreshToken mockToken = Mocks.refreshToken(10000);

    private Strategy mockStrategy = Mocks.strategy();

    private CbotStatus mockStatus = Mocks.cbotStatus();

    @BeforeEach
    public void setup() {
        repo.deleteAll();
        mockUser.setCards(Mocks.cards());
        repo.save(mockUser);
        userService = new UserService(repo);
    }

    @Test
    void shouldAddCardToUser() {
        userService.addCard(mockUser, Mocks.card());
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getCards().get(Mocks.card().getCardName()),
                samePropertyValuesAs(Mocks.card(), "balances"));
    }

    @Test
    void shouldThrowUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserWithUsername("mockUser4"));
    }

    @Test
    void shouldReplaceUsers(){
        mockUser.setRefreshToken(mockToken);

        userService.replace(mockUser);

        assertThat(repo.getUserByUsername(mockUser.getUsername()).getRefreshToken(), samePropertyValuesAs(mockToken));
    }

    @Test
    void shouldAddStrategyToUser() {
        userService.addStrategy(mockUser, mockStrategy);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getStrategies().get(mockStrategy.getName()),
                samePropertyValuesAs(mockStrategy));
    }

    @Test
    void shouldChangeUserStatus(){
        userService.updateStatus(mockUser, mockStatus);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getCbotStatus(), samePropertyValuesAs(mockStatus));
    }

}
