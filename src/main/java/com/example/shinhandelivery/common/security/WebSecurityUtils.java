package com.example.shinhandelivery.common.security;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.ui.Model;

/** Web Controller에서 사용자 인증 ID 추출 및 안전한 Model 바인딩(예외 캡슐화)을 위한 헬퍼 유틸리티입니다. */
public final class WebSecurityUtils {

  private WebSecurityUtils() {}

  /** CustomUserDetails 객체로부터 인증된 사용자 ID(memberId)를 안전하게 Optional로 추출합니다. */
  public static Optional<Long> getUserId(CustomUserDetails userDetails) {
    return Optional.ofNullable(userDetails).map(CustomUserDetails::getId);
  }

  /** 예외 발생 시 무시하고 안전하게 실행하는 헬퍼 메서드입니다. */
  public static void safeExecute(Runnable action) {
    try {
      action.run();
    } catch (Exception ignored) {
    }
  }

  /** 인증된 사용자가 존재할 경우 안전하게 예외 없이 액션을 집행합니다. */
  public static void ifAuthenticated(CustomUserDetails userDetails, Consumer<Long> action) {
    getUserId(userDetails).ifPresent(userId -> safeExecute(() -> action.accept(userId)));
  }

  /** Model에 특정 키값의 데이터를 안전하게 추가합니다 (예외 발생 시 무시 및 fallback). */
  public static <T> void safeAddAttribute(Model model, String attributeName, Supplier<T> supplier) {
    try {
      T value = supplier.get();
      if (value != null) {
        model.addAttribute(attributeName, value);
      }
    } catch (Exception ignored) {
    }
  }
}
