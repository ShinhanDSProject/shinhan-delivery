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
}
