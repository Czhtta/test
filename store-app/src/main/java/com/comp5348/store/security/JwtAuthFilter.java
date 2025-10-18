package com.comp5348.store.security;

import com.comp5348.store.security.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 从请求中获取JWT
            String jwt = parseJwt(request);

            // 2. 验证JWT
            if (jwt != null && jwtProvider.validateToken(jwt)) {

                // 3. 从Token中获取用户名
                String username = jwtProvider.getUsernameFromToken(jwt);

                // 4. 加载用户信息 (UserDetails)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 创建一个 "认证凭证"
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // 我们不需要密码
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 将凭证设置到 Spring Security 的“安全上下文”中
                // 这行代码就相当于告诉Spring Security：“这个用户已经登录了！”
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // (可选) 记录认证失败的日志
            // logger.error("Cannot set user authentication: {}", e);
        }

        // 7. 无论是否认证成功，都继续执行过滤链的下一个过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求的 "Authorization" Header 中提取 Bearer Token
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // 检查Header是否存在，并且是否以 "Bearer " 开头
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // 返回 "Bearer " 之后的部分，即Token本身
            return headerAuth.substring(7);
        }

        return null; // 没有找到Token
    }
}
