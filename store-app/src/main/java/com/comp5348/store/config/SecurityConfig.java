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
                        // 只有管理员可以访问 /api/admin/
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        // 只有管理员可以 C/U/D 商品
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        // 任何人都可以查看商品
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll()
                        // 订单API的精细控制
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders").hasAuthority("ROLE_ADMIN") // Admin看所有订单
                        .requestMatchers("/api/orders/**").hasAuthority("ROLE_USER") // 普通用户操作自己的订单
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
