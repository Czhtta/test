package com.comp5348.emailapp;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class EmailAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailAppApplication.class, args);
    }

}
