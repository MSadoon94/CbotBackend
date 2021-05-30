package com.sadoon.cbotback.cryptoprofile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class CryptoProfileControllerTest {

    private static PasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final List<CryptoProfile> TEST_PROFILES = List.of(
            new CryptoProfile("profile1", encoder.encode("pass1"), new SimpleGrantedAuthority("USER")),
            new CryptoProfile("profile2", encoder.encode("pass2"), new SimpleGrantedAuthority("USER"))
    );


    @Autowired
    private CryptoProfileRepository repository;

    private CryptoProfileController controller;

    @BeforeEach
    void setUp(){
        repository.deleteAll();
        repository.saveAll(TEST_PROFILES);
        controller = new CryptoProfileController(repository);
    }

    @Test
    void shouldCreateCryptoProfile(){
        assertThat(controller.addProfile(TEST_PROFILES.get(0)).getId(), is(TEST_PROFILES.get(0).getId()));
    }

    @Test
    void shouldGetSpecificCryptoProfile(){
        assertThat(controller.getProfile("profile1").getId(), is(TEST_PROFILES.get(0).getId()));
    }
}
