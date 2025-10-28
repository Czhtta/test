package com.comp5348.store.service;

import com.comp5348.store.entity.User;
import com.comp5348.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param password 原始密码
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

    /**
     * 创建新用户（如果需要）
     * @param username 用户名
     * @param password 原始密码
     * @param role 角色 (例如 "ROLE_USER", "ROLE_ADMIN")
     * @param address 地址
     * @return 创建的用户实体
     */
    @Transactional
    public User createUser(String username, String password, String role, String address, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) { // 假设你在 UserRepository 添加了 findByEmail 方法
            throw new RuntimeException("Email already exists: " + email);
        }
        User user = new User();
        // 密码进行哈希
        String hashedPassword = passwordEncoder.encode(password);
        user.setUsername(username);
        user.setPassword(hashedPassword); // 保存哈希后的密码
        user.setRole(role);
        user.setAddress(address);
        return userRepository.save(user);
    }

}
