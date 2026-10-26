package com.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FactoryCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(FactoryCoreApplication.class, args);
    }
}
