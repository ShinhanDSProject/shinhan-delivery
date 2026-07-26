package com.example.shinhangaecheokja.auth.jwt;

import com.example.shinhangaecheokja.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpirationMinutes;

    public JwtTokenProvider(
            @Value("${jwt.member.secret}") String secret,
            @Value("${jwt.member.access-exp-min}") long accessExpirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMinutes = accessExpirationMinutes;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationMinutes * 60;
    }
}
