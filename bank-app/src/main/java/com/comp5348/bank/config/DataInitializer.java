package com.comp5348.bank.config;

import com.comp5348.bank.entity.Account;
import com.comp5348.bank.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Initialize test accounts for the bank system
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(AccountRepository repository) {
        return args -> {
            // Create store account
            if (!repository.existsByAccountNumber("STORE001")) {
                Account storeAccount = new Account(
                        "STORE001",
                        "Online Store",
                        new BigDecimal("100000.00"),
                        "STORE"
                );
                repository.save(storeAccount);
                logger.info("Created store account: STORE001 with balance $100,000");
            }

            // Create customer account for testing (username: customer, password: COMP5348)
            if (!repository.existsByAccountNumber("CUST001")) {
                Account customerAccount = new Account(
                        "CUST001",
                        "customer",  // username as required
                        new BigDecimal("10000.00"),
                        "CUSTOMER"
                );
                repository.save(customerAccount);
                logger.info("Created customer account: CUST001 with balance $10,000");
            }

            // Create additional test customers
            if (!repository.existsByAccountNumber("CUST002")) {
                Account customerAccount2 = new Account(
                        "CUST002",
                        "John Smith",
                        new BigDecimal("5000.00"),
                        "CUSTOMER"
                );
                repository.save(customerAccount2);
                logger.info("Created customer account: CUST002 with balance $5,000");
            }

            if (!repository.existsByAccountNumber("CUST003")) {
                Account customerAccount3 = new Account(
                        "CUST003",
                        "Jane Doe",
                        new BigDecimal("8000.00"),
                        "CUSTOMER"
                );
                repository.save(customerAccount3);
                logger.info("Created customer account: CUST003 with balance $8,000");
            }

            logger.info("Bank database initialization completed");
        };
    }
}