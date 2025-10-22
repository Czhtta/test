package com.comp5348.store.config;

import com.comp5348.store.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用CSRF保护，因为我们使用JWT，不需要Session
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置会话管理为“无状态”(STATELESS)
                // 这告诉Spring Security不要创建或使用HTTP Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 配置URL的访问权限
                .authorizeHttpRequests(authz -> authz
                        // 允许任何人(permitAll)访问所有以 /api/auth/ 开头的URL
                        .requestMatchers("/api/auth/**").permitAll()
                        // message测试临时加一下
                        .requestMatchers("/api/test/**").permitAll()
                        // 除了上面放行的URL外，其他所有请求(anyRequest)都必须经过认证(authenticated)
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 4. 构建并返回配置好的SecurityFilterChain
        return http.build();
    }

}
