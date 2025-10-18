package com.comp5348.store.controller;

import com.comp5348.store.security.JwtProvider;
import com.comp5348.store.service.UserService;
import com.comp5348.store.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @RequestMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        boolean isAuthenticated = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (isAuthenticated) {
            String token = jwtProvider.generateToken(loginRequest.getUsername());
            Map<String, String> response = Map.of("token", token);
            return ResponseEntity.ok(response);
        }else{
            Map<String, String> response = Map.of("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
