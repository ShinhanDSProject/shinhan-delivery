package com.example.shinhandelivery.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String SECRET =
      "shinhan-delivery-super-secret-key-for-jwt-authentication-token-security-2026-very-long";

  private JwtProvider jwtProvider;
  private JwtAuthenticationFilter filter;
  private String accessToken;

  @BeforeEach
  void setUp() {
    jwtProvider = new JwtProvider(SECRET, 3600000L, 1209600000L);
    filter = new JwtAuthenticationFilter(jwtProvider);
    accessToken = jwtProvider.createAccessToken(1L, "user@example.com", "CUSTOMER");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Authorization 헤더가 있으면 헤더의 토큰으로 인증한다")
  void authenticatesFromAuthorizationHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    request.addHeader("Authorization", "Bearer " + accessToken);

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test
  @DisplayName("헤더가 없고 SSR 페이지 경로면 accessToken 쿠키로 폴백 인증한다")
  void authenticatesFromCookieForNonApiPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    request.setCookies(new Cookie("accessToken", accessToken));

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test
  @DisplayName("헤더가 없고 REST API 경로면 accessToken 쿠키가 있어도 인증하지 않는다")
  void doesNotAuthenticateFromCookieForApiPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/members/me");
    request.setCookies(new Cookie("accessToken", accessToken));

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("헤더도 쿠키도 없으면 인증하지 않는다")
  void doesNotAuthenticateWithoutTokenSource() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
