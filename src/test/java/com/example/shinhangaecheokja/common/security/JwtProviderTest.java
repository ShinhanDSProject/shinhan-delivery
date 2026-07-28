package com.example.shinhangaecheokja.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class JwtProviderTest {

  private JwtProvider jwtProvider;

  @BeforeEach
  void setUp() {
    String secret =
        "shinhan-gaecheokja-super-secret-key-for-jwt-authentication-token-security-2026-very-long";
    jwtProvider = new JwtProvider(secret, 3600000L, 1209600000L);
  }

  @Test
  @DisplayName("Access Token 생성 및 파싱 검증")
  void createAndValidateAccessToken() {
    // given
    Long id = 1L;
    String email = "test@example.com";
    String role = "CUSTOMER";

    // when
    String token = jwtProvider.createAccessToken(id, email, role);

    // then
    assertThat(token).isNotNull();
    assertThat(jwtProvider.validateToken(token)).isTrue();

    Claims claims = jwtProvider.parseClaims(token);
    assertThat(claims.getSubject()).isEqualTo(email);
    assertThat(claims.get("id", Long.class)).isEqualTo(id);
    assertThat(claims.get("role", String.class)).isEqualTo(role);
  }

  @Test
  @DisplayName("JWT 토큰 기반 Authentication 객체 추출 검증")
  void getAuthenticationFromToken() {
    // given
    Long id = 2L;
    String email = "driver@example.com";
    String role = "DRIVER";
    String token = jwtProvider.createAccessToken(id, email, role);

    // when
    Authentication authentication = jwtProvider.getAuthentication(token);

    // then
    assertThat(authentication).isNotNull();
    assertThat(authentication.getName()).isEqualTo(email);
    assertThat(authentication.getAuthorities())
        .anyMatch(a -> "ROLE_DRIVER".equals(a.getAuthority()));
  }

  @Test
  @DisplayName("변조된 토큰 검증 시 false 반환")
  void validateInvalidToken() {
    // given
    String invalidToken = "invalid.jwt.token.value";

    // when & then
    assertThat(jwtProvider.validateToken(invalidToken)).isFalse();
  }
}
