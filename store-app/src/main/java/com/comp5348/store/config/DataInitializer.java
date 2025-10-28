package com.comp5348.store.config; // 确认包名正确

import com.comp5348.store.entity.User;
import com.comp5348.store.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // 导入 PasswordEncoder

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository; // 注入 UserRepository

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入 PasswordEncoder

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // 创建 demonstration 用户: customer / COMP5348
            if (!userRepository.existsByUsername("customer")) {
                User demoUser = new User();
                demoUser.setUsername("customer");
                // --- 使用 passwordEncoder 对密码进行哈希 ---
                demoUser.setPassword(passwordEncoder.encode("COMP5348"));
                demoUser.setRole("ROLE_USER");
                demoUser.setAddress("500 Demo St, Sydney");
                demoUser.setEmail("customer@example.com");
                demoUser.setBankAccountNumber("CUST001");
                userRepository.save(demoUser);
                logger.info("Created demonstration user: customer");
            } else {
                logger.info("User 'customer' already exists.");
            }

            // 创建 admin 用户: admin / password
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("password"));
                adminUser.setRole("ROLE_ADMIN");
                adminUser.setAddress("1 Admin Road, Sydney");
                adminUser.setEmail("admin@example.com");
                adminUser.setBankAccountNumber("CUST002");
                userRepository.save(adminUser);
                logger.info("Created admin user: admin");
            } else {
                logger.info("User 'admin' already exists.");
            }

            // 创建普通 user: user / password
            if (!userRepository.existsByUsername("user")) {
                User normalUser = new User();
                normalUser.setUsername("user");
                normalUser.setPassword(passwordEncoder.encode("password")); // 使用哈希后的密码
                normalUser.setRole("ROLE_USER");
                normalUser.setAddress("100 User Ave, Melbourne");
                normalUser.setEmail("user@example.com");
                normalUser.setBankAccountNumber("CUST003");
                userRepository.save(normalUser);
                logger.info("Created normal user: user");
            } else {
                logger.info("User 'user' already exists.");
            }


            logger.info("User data initialization completed.");
        };
    }
}