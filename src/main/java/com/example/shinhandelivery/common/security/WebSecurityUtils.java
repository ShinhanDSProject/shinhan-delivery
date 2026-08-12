package com.example.shinhandelivery.common.security;

import java.util.Optional;

/** Web Controller에서 사용자 인증 ID를 안전하게 추출하기 위한 헬퍼 유틸리티입니다. */
public final class WebSecurityUtils {

  private WebSecurityUtils() {}

  /** CustomUserDetails 객체로부터 인증된 사용자 ID(memberId)를 안전하게 Optional로 추출합니다. */
  public static Optional<Long> getUserId(CustomUserDetails userDetails) {
    return Optional.ofNullable(userDetails).map(CustomUserDetails::getId);
  }
}
