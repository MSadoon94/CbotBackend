package com.sadoon.cbotback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.duplication.DuplicateUserException;
import com.sadoon.cbotback.user.models.SignUpRequest;
import com.sadoon.cbotback.util.DuplicateHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SignUpControllerTest {

    private MockMvc mvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SignUpRequest mockRequest = Mocks.signUpRequest();

    @Mock
    private DuplicateHandler duplicateHandler;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private SignUpController controller;

    @BeforeEach
    public void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCreatedStatusOnSignUpSuccess() throws Exception {
        signUp()
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnConflictStatusOnSignUpFailFromDuplicateUsername() throws Exception{
        doThrow(new DuplicateUserException(
                        "user",
                        String.format(
                                "with the username '%s', please choose another username.",
                                mockRequest.getUsername())
                        )
        ).when(duplicateHandler).checkForExistingUser(any());

        signUp()
                .andExpect(status().isConflict());
    }

    private ResultActions signUp() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)));
    }

}
