package com.example.shinhandelivery.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtProvider(
      @Value(
              "${jwt.secret:shinhan-delivery-super-secret-key-for-jwt-authentication-token-security-2026-very-long}")
          String secret,
      @Value("${jwt.access-token-expiration:3600000}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration:1209600000}") long refreshTokenExpiration) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public long getAccessTokenExpirationSeconds() {
    return accessTokenExpiration / 1000;
  }

  public String createAccessToken(Long id, String email, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .subject(email)
        .claim("id", id)
        .claim("role", role)
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey)
        .compact();
  }

  public String createRefreshToken(Long id, String email) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .subject(email)
        .claim("id", id)
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);
    Long id = claims.get("id", Long.class);
    String email = claims.getSubject();
    String role = claims.get("role", String.class);

    CustomUserDetails principal =
        new CustomUserDetails(id, email, "", role != null ? role : "CUSTOMER");
    return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
