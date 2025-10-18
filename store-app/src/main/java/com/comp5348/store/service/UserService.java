package com.comp5348.store.service;

import com.comp5348.store.entity.User;
import com.comp5348.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用于认证用户
     * @param username 用户名
     * @param password 原始密码 (如 "COMP5348")
     * @return 如果认证成功，返回true，否则false
     */
    public boolean authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return false; // 用户不存在，认证失败
        }

        User user = userOptional.get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User createUser(String username, String password) {
        User user = new User();
        String hashedPassword = passwordEncoder.encode(password);
        user.setUsername(username);
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

}
