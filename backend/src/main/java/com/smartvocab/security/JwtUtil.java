package com.smartvocab.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具
 */
@Component
public class JwtUtil {

    @Value("${smartvocab.jwt.secret}")
    private String secret;

    @Value("${smartvocab.jwt.expire-hours}")
    private long expireHours;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(Long userId, String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Map<String, Object> summary(Claims c) {
        return Map.of("userId", Long.valueOf(c.getSubject()), "email", c.get("email"));
    }
}
