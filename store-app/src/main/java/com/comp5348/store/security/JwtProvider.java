package com.comp5348.store.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;

import java.security.Key;
import java.util.Date;

@Service
public class JwtProvider {

    private final String secretKey;
    private final long expirationMs;

    // 1. 从 application.properties 中读取密钥和过期时间
    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration.ms}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    /**
     * 为指定用户生成一个新的JWT
     * @param username 登录的用户名
     * @return JWT字符串
     */
    public String generateToken(String username) {
        // Token的过期时间
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // 2. 使用 Jwts.builder() 来创建Token
        return Jwts.builder()
                .setSubject(username) // 1. "Subject" (主题)，我们存入用户名
                .setIssuedAt(now)     // 2. "Issued At" (签发时间)
                .setExpiration(expiryDate) // 3. "Expiration" (过期时间)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 4. "Sign" (签名)
                .compact(); // 5. 构建并序列化为字符串
    }

    /**
     * 从Base64编码的密钥字符串生成一个 Key 对象
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从Token中解析出用户名 (Subject)
     * @param token JWT字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        // 1. 使用你的密钥创建一个JWT解析器
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();

        // 2. 解析Token，获取"claims" (Token中存储的数据)
        Jws<Claims> claimsJws = parser.parseClaimsJws(token);

        // 3. 返回 "subject" 字段，我们存的就是用户名
        return claimsJws.getBody().getSubject();
    }

    /**
     * 验证Token是否有效
     * @param token JWT字符串
     * @return true 如果有效, false 如果无效 (如过期、签名错误)
     */
    public boolean validateToken(String token) {
        try {
            // 只要解析成功，没有抛出异常，就说明Token是有效的
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // (可选) 在这里打印日志，查看Token无效的原因
            // 例如: MalformedJwtException, ExpiredJwtException, SignatureException
            // logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

}
