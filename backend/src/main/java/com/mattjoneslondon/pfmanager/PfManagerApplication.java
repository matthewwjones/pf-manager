package com.mattjoneslondon.pfmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PfManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PfManagerApplication.class, args);
    }
}