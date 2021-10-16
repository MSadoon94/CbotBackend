package com.sadoon.cbotback.user;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
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

    @BeforeEach
    public void setup() {
        repo.deleteAll();
        mockUser.setCards(Mocks.cardList());
        repo.save(mockUser);
        userService = new UserService(repo);
    }

    @Test
    void shouldAddCardsToUser() {
        userService.addCard(mockUser, Mocks.card());
        assertThat(repo.getUserByUsername("mockUser").getCards().get(2), samePropertyValuesAs(Mocks.card()));
    }

    @Test
    void shouldThrowUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.getUser("mockUser4"));
    }
}
