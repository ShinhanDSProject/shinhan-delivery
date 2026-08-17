package com.example.shinhandelivery.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

/** JWT 쿠키 발급, 무효화 및 추출을 전담하는 유틸리티 클래스입니다. */
public final class CookieUtils {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  public static final long DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS = 3600L;
  public static final long DEFAULT_REFRESH_TOKEN_MAX_AGE_SECONDS = 1209600L;

  private CookieUtils() {}

  /** Access Token용 HttpOnly 및 SameSite=Lax ResponseCookie를 생성합니다. */
  public static ResponseCookie createAccessTokenCookie(String token) {
    return createAccessTokenCookie(token, DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS);
  }

  /** 지정된 만료 시간(초)을 갖는 Access Token ResponseCookie를 생성합니다. */
  public static ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds) {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token != null ? token : "")
        .httpOnly(true)
        .path("/")
        .maxAge(maxAgeSeconds)
        .sameSite("Lax")
        .build();
  }

  /** Refresh Token용 HttpOnly 및 SameSite=Lax ResponseCookie를 생성합니다. */
  public static ResponseCookie createRefreshTokenCookie(String token) {
    return createRefreshTokenCookie(token, DEFAULT_REFRESH_TOKEN_MAX_AGE_SECONDS);
  }

  /** 지정된 만료 시간(초)을 갖는 Refresh Token ResponseCookie를 생성합니다. */
  public static ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token != null ? token : "")
        .httpOnly(true)
        .path("/")
        .maxAge(maxAgeSeconds)
        .sameSite("Lax")
        .build();
  }

  /** 특정 쿠키를 만료(삭제)시키는 Max-Age=0 ResponseCookie를 생성합니다. */
  public static ResponseCookie createClearCookie(String cookieName) {
    return ResponseCookie.from(cookieName, "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
  }

  /** HttpServletRequest에서 지정된 이름의 쿠키 값을 안전하게 추출합니다. */
  public static String extractCookieValue(HttpServletRequest request, String cookieName) {
    if (request == null || !StringUtils.hasText(cookieName)) {
      return null;
    }
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        String value = cookie.getValue();
        return StringUtils.hasText(value) ? value : null;
      }
    }
    return null;
  }
}
