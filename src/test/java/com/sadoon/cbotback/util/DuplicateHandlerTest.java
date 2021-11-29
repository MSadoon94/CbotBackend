package com.sadoon.cbotback.util;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.duplication.DuplicateUserException;
import com.sadoon.cbotback.user.UserRepository;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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
        given(mockRepo.getUserByUsername(any())).willReturn(null);

        assertThrows(DuplicateUserException.class,
                () -> duplicateHandler.checkForExistingUser(mockUser.getUsername()));
    }
}
