package com.comp5348.bank;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bank Application for COMP5348 Group Project
 */
@EnableRabbit  // ✅ 启用 RabbitMQ 监听（让 @RabbitListener 生效）
@SpringBootApplication
public class BankAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAppApplication.class, args);
    }
}
