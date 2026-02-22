package com.example.idp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdpApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdpApplication.class, args);
    }
}
