package com.sadoon.cbotback.cryptoprofile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CryptoProfileControllerTest {

    private static final List<CryptoProfile> TEST_PROFILES = List.of(
            new CryptoProfile("profile1", "pass1"),
            new CryptoProfile("profile2", "pass2")
    );

    @Autowired
    private CryptoProfileRepository repository;

    @BeforeEach
    void setUp(){
        repository.deleteAll();
    }

    @Test
    void shouldCreateCryptoProfile(){
        CryptoProfileController controller = new CryptoProfileController(repository);

        assertThat(controller.add(TEST_PROFILES.get(0)), is(TEST_PROFILES.get(0)));
    }
    
}
