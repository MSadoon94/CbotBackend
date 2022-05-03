package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.structure.ExchangeUtil;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@DataMongoTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Autowired
    private UserRepository repo;

    @Mock
    private ExchangeUtil mockExchangeUtil;

    private User mockUser = Mocks.user();
    private UserService userService;

    private RefreshToken mockToken = Mocks.refreshToken(10000);

    private Strategy mockStrategy = Mocks.strategy();
    private Map<String, Strategy> strategies = new LinkedHashMap<>();

    private CbotStatus mockStatus = Mocks.cbotStatus();

    private SecurityCredential mockCredentials = new SecurityCredential(
            ExchangeName.KRAKEN.name(),
            "mockAccount",
            "mockPassword"
    );

    @BeforeEach
    public void setup() {
        repo.deleteAll();
        mockUser.setCards(Mocks.cards());
        mockUser.addExchange(ExchangeName.KRAKEN);

        strategies.put(mockStrategy.getName(), mockStrategy);

        mockUser.setStrategies(strategies);

        repo.save(mockUser);
        userService = new UserService(repo);
    }

    @Test
    void shouldThrowUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserWithUsername("mockUser4"));
    }

    @Test
    void shouldReplaceUsers() {
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
    void shouldChangeUserStatus() {
        userService.updateStatus(mockUser, mockStatus);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getCbotStatus(), samePropertyValuesAs(mockStatus));
    }

    @Test
    void shouldAddEncryptedCredentialToUser() {

        userService.addEncryptedCredential(mockUser, mockCredentials);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getEncryptedCredential(mockCredentials.type()),
                samePropertyValuesAs(mockCredentials)
        );
    }
}
