package com.comp5348.deliverycoapp;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
/**
 * @EnableAsync 开启Spring的异步方法执行功能, 用于模拟耗时的配送过程.
 */
@EnableAsync
@EnableRabbit
@SpringBootApplication
public class DeliveryCoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryCoAppApplication.class, args);
    }

}