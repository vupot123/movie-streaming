package com.example.movie_streaming.common.security;

import com.example.movie_streaming.common.configuration.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    private final JwtProperties jwtProperties;
    private Key key;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        // Tạo key từ chuỗi bí mật
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Sinh JWT token cho username
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy username từ token
     */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            logger.error("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported token: {}", e.getMessage());
            throw new RuntimeException("Unsupported token");
        } catch (MalformedJwtException e) {
            logger.error("Malformed token: {}", e.getMessage());
            throw new RuntimeException("Malformed token");
        } catch (SecurityException | IllegalArgumentException e) {
            logger.error("Invalid token signature: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }
}
