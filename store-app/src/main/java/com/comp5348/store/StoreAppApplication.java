package com.comp5348.store;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class StoreAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreAppApplication.class, args);
    }

}
