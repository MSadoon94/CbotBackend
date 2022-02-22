package com.sadoon.cbotback.util;

import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.duplication.DuplicateUserException;
import com.sadoon.cbotback.user.UserRepository;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DuplicateHandlerTest {

    @Mock
    private UserRepository mockRepo;

    private User mockUser = Mocks.user();

    private DuplicateHandler duplicateHandler;

    @BeforeEach
    public void setUp(){
        duplicateHandler = new DuplicateHandler(mockRepo);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsFound(){
        given(mockRepo.getUserByUsername(any())).willReturn(mockUser);

        assertThrows(DuplicateUserException.class,
                () -> duplicateHandler.checkForExistingUser(mockUser.getUsername()));
    }
}
