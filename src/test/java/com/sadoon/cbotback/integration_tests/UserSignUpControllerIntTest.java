package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.user.UserRepository;
import com.sadoon.cbotback.user.UserSignUpController;
import com.sadoon.cbotback.user.models.SignUpRequest;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@ActiveProfiles("test")
class UserSignUpControllerIntTest {

    private static final SignUpRequest TEST_REQUEST =
            new SignUpRequest(
                    "TestUser",
                    "password",
                    "USER"
            );
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired
    private UserRepository repo;

    private UserSignUpController controller;

    private ResponseEntity<?> response;

    private User user;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        controller = new UserSignUpController(repo);
        response = controller.addUser(TEST_REQUEST);
        user = repo.getUserByUsername(TEST_REQUEST.getUsername());

    }

    @Test
    void shouldCreateNewUser() {
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void shouldPassRequestUsernameToUser() {
        assertThat(user.getUsername(), is(TEST_REQUEST.getUsername()));
    }

    @Test
    void shouldPassRequestPasswordToUser() {
        assertThat(encoder.matches(TEST_REQUEST.getPassword(), user.getPassword()), is(true));
    }

    @Test
    void shouldPassRequestAuthorityToUser() {
        assertThat(user.getAuthorities().get(0), is(TEST_REQUEST.getAuthority()));
    }

}
