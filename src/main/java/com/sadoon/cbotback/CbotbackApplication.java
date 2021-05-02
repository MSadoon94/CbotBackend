package com.sadoon.cbotback;

import com.sadoon.cbotback.cryptoprofile.CryptoProfileRepository;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableEncryptableProperties
public class CbotbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbotbackApplication.class, args);
    }

}
