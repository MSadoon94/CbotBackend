package com.sadoon.cbotback.user.integration;
import com.sadoon.cbotback.security.jwt.JwtUtil;
import com.sadoon.cbotback.user.UserLoginController;
import com.sadoon.cbotback.user.UserRepository;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class UserLoginControllerIntTest {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final User TEST_USER =
            new User(
                    "TestUser",
                    encoder.encode("password"),
                    new SimpleGrantedAuthority("USER")
            );

    private static final LoginRequest TEST_REQUEST =
            new LoginRequest(TEST_USER.getUsername(), TEST_USER.getPassword());

    @Autowired
    private UserRepository repo;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager manager;

    @MockBean
    private JwtUtil jwtUtil;

    private UserLoginController controller;

    @BeforeEach
    void setUp(){
        repo.deleteAll();
        repo.save(TEST_USER);
        controller = new UserLoginController(manager, jwtUtil, userDetailsService);
    }

    @Test
    void shouldCreateJwtForLoginWithCorrectCredentials(){
        String jwt = jwtUtil.generateToken(userDetailsService.loadUserByUsername(TEST_USER.getUsername()));

        LoginResponse response = controller.login(TEST_REQUEST).getBody();

        assert response != null;
        assertThat(response.getJwt(), is(equalTo(jwt)));
    }
}
