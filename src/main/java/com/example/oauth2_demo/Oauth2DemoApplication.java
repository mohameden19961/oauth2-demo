package com.example.oauth2_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Oauth2DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(Oauth2DemoApplication.class, args);
    }
}
