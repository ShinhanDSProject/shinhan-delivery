package com.example.shinhandelivery.common.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Web Controller에서 로그인 사용자의 세션/인증 정보를 보일러플레이트 없이 안전하게 추출하는 공통 헬퍼입니다. */
@Component
public class WebAuthHelper {

  public Optional<CustomUserDetails> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
      return Optional.of(userDetails);
    }
    return Optional.empty();
  }

  public Optional<Long> getCurrentMemberId() {
    return getCurrentUser().map(CustomUserDetails::getId);
  }

  /** 로그인한 사용자가 주어진 역할(예: "COURIER")을 갖고 있는지 확인합니다. 비로그인 사용자는 false입니다. */
  public boolean currentUserHasRole(String role) {
    return getCurrentUser()
        .map(
            user ->
                user.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
        .orElse(false);
  }

  /** 로그인한 사용자의 역할에 맞는 홈 경로를 반환합니다. 비로그인·ADMIN·CUSTOMER는 "/home"으로 취급합니다. */
  public String getHomePath() {
    return currentUserHasRole("COURIER") ? "/courier-home" : "/home";
  }
}
