package com.sadoon.cbotback;

import com.sadoon.cbotback.util.ExchangeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, ExchangeProperties.class})
public class CbotbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbotbackApplication.class, args);
    }
}
