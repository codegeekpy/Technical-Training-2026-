package com.anurag.events.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-minutes}")
    private int expirationMinutes;

    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 needs at least 32 bytes; pad if needed
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String role) {
        long nowMs = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date(nowMs))
                .expiration(new Date(nowMs + (long) expirationMinutes * 60 * 1000))
                .signWith(getKey())
                .compact();
    }

    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(validateAndExtract(token).getSubject());
    }

    public String getRole(String token) {
        return validateAndExtract(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            validateAndExtract(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
