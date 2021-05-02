package com.sadoon.cbotback.cryptoprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CryptoProfileRepositoryTest {

    private static final List<CryptoProfile> TEST_PROFILES = List.of(
            new CryptoProfile("profile1", "pass1"),
            new CryptoProfile("profile2", "pass2")
    );

    @Autowired
    private CryptoProfileRepository repository;
    @BeforeEach
    void setUp(){
        repository.deleteAll();
        repository.saveAll(TEST_PROFILES);
    }
    @Test
    void shouldStoreProfiles(){

        assertThat(repository.count(), is(equalTo((long) TEST_PROFILES.size())));
    }

}
