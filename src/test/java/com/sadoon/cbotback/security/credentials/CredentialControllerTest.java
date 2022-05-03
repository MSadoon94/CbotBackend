package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.update.UserUpdater;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CredentialControllerTest {

    @Mock
    private UserUpdater userUpdater;
    @Mock
    private CredentialsService credentialsService;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private SecurityCredential mockCredentials
            = new SecurityCredential(
            ExchangeName.KRAKEN.name(),
            "mockAccount",
            "mockPassword"
    );

    @InjectMocks
    private CredentialController controller;

    @Test
    void shouldReturnSuccessMessageOnCredentialAddition() throws Exception {
        WebSocketTest webSocketTest
                = new WebSocketTest(controller, new SimpMessagingTemplate(new TestMessageChannel()));

        webSocketTest.sendMessageToController(webSocketTest.sendHeaderAccessor(
                        "/app/add-credentials", auth),
                Mocks.mapper.writeValueAsBytes(mockCredentials)
        );

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);

        assertThat(reply.getPayload(), is("KRAKEN added successfully."));
    }

}
